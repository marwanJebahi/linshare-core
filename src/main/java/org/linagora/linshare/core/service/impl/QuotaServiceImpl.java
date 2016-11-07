/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2015. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.core.service.impl;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.AccountQuotaBusinessService;
import org.linagora.linshare.core.business.service.DomainQuotaBusinessService;
import org.linagora.linshare.core.business.service.EnsembleQuotaBusinessService;
import org.linagora.linshare.core.business.service.OperationHistoryBusinessService;
import org.linagora.linshare.core.domain.constants.ContainerQuotaType;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.AccountQuota;
import org.linagora.linshare.core.domain.entities.ContainerQuota;
import org.linagora.linshare.core.domain.entities.DomainQuota;
import org.linagora.linshare.core.domain.entities.Quota;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.rac.QuotaResourceAccessControl;
import org.linagora.linshare.core.service.QuotaService;

public class QuotaServiceImpl extends GenericServiceImpl<Account, Quota> implements QuotaService {

	private AccountQuotaBusinessService accountQuotaBusinessService;
	private DomainQuotaBusinessService domainQuotaBusinessService;
	private EnsembleQuotaBusinessService ensembleQuotaBusinessService;
	private OperationHistoryBusinessService operationHistoryBusinessService;

	public QuotaServiceImpl(
			QuotaResourceAccessControl rac,
			AccountQuotaBusinessService accountQuotaBusinessService,
			DomainQuotaBusinessService domainQuotaBusinessService,
			EnsembleQuotaBusinessService ensembleQuotaBusinessService,
			OperationHistoryBusinessService operationHistoryBusinessService) {
		super(rac);
		this.accountQuotaBusinessService = accountQuotaBusinessService;
		this.domainQuotaBusinessService = domainQuotaBusinessService;
		this.ensembleQuotaBusinessService = ensembleQuotaBusinessService;
		this.operationHistoryBusinessService = operationHistoryBusinessService;
	}

	@Override
	public void checkIfUserCanAddFile(Account actor, Account owner, Long fileSize, ContainerQuotaType containerQuotaType)
			throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		Validate.notNull(owner, "Owner must be set.");
		Validate.notNull(fileSize, "FileSize must be set.");
		Validate.notNull(containerQuotaType, "EnsembleType must be set.");
		checkReadPermission(actor, owner, Quota.class, BusinessErrorCode.QUOTA_UNAUTHORIZED, null);
		checkIfUserCanAddInAccountQuota(owner, fileSize);
		checkIfUserCanAddInContainerQuota(owner.getDomain(), containerQuotaType, fileSize);
		checkIfUserCanAddInDomainQuota(owner.getDomain(), fileSize);
		checkIfUserCanAddInPlatformQuota(fileSize);
	}

	private void checkIfUserCanAddInAccountQuota(Account account, Long fileSize) throws BusinessException {
		Validate.notNull(account, "Account must be set.");
		Validate.notNull(fileSize, "File size must be set.");
		AccountQuota aq = accountQuotaBusinessService.find(account);
		if (aq == null) {
			throw new BusinessException(BusinessErrorCode.ACCOUNT_QUOTA_NOT_FOUND,
					"The account quota is not configured yet.");
		}
		if (fileSize > aq.getFileSizeMax()) {
			throw new BusinessException(BusinessErrorCode.QUOTA_FILE_UNAUTHORIZED,
					"The file size is greater than the file quota.");
		}
		Long todayConsumption = operationHistoryBusinessService.sumOperationValue(account, null, new Date(), null, null);
		Long totalConsumption = aq.getCurrentValue() + todayConsumption + fileSize;
		if (totalConsumption > aq.getQuota()) {
			throw new BusinessException(BusinessErrorCode.QUOTA_ACCOUNT_UNAUTHORIZED,
					"The account quota has been reached.");
		}
	}

	private void checkIfUserCanAddInDomainQuota(AbstractDomain domain, Long fileSize) throws BusinessException {
		Validate.notNull(domain, "Domain must be set.");
		Validate.notNull(fileSize, "File size must be set.");
		DomainQuota dq = domainQuotaBusinessService.find(domain);
		if (dq == null) {
			throw new BusinessException(BusinessErrorCode.DOMAIN_QUOTA_NOT_FOUND,
					"The domain quota is not configured yet.");
		}
		Long todayConsumption = operationHistoryBusinessService.sumOperationValue(null, domain, new Date(), null,
				null);
		Long totalConsumption = dq.getCurrentValue() + todayConsumption + fileSize;
		if (totalConsumption > dq.getQuota()) {
			throw new BusinessException(BusinessErrorCode.QUOTA_DOMAIN_UNAUTHORIZED,
					"The domain quota has been reached.");
		}
	}

	private void checkIfUserCanAddInContainerQuota(AbstractDomain domain, ContainerQuotaType containerQuotaType, Long fileSize)
			throws BusinessException {
		Validate.notNull(domain, "Domain must be set.");
		Validate.notNull(fileSize, "File size must be set.");
		ContainerQuota cq = ensembleQuotaBusinessService.find(domain, containerQuotaType);
		if (cq == null) {
			throw new BusinessException(BusinessErrorCode.CONTAINER_QUOTA_NOT_FOUND,
					"The container quota is not configured yet.");
		}
		Long todayConsumption = operationHistoryBusinessService.sumOperationValue(null, domain, new Date(), null,
				containerQuotaType);
		Long totalConsumption = cq.getCurrentValue() + todayConsumption + fileSize;
		if (totalConsumption > cq.getQuota()) {
			throw new BusinessException(BusinessErrorCode.QUOTA_CONTAINER_UNAUTHORIZED,
					"The container quota has been reached.");
		}
	}

	private void checkIfUserCanAddInPlatformQuota(Long fileSize) throws BusinessException {
		Validate.notNull(fileSize, "File size must be set.");
		DomainQuota pq = domainQuotaBusinessService.findRootQuota();
		if (pq == null) {
			throw new BusinessException(BusinessErrorCode.QUOTA_PLATFORM_UNAUTHORIZED, "The platform quota is not configured yet.");
		}
		Long todayConsumption = operationHistoryBusinessService.sumOperationValue(null, null, new Date(), null,
				null);
		Long totalConsumption = pq.getCurrentValue() + todayConsumption + fileSize;
		if (totalConsumption > pq.getQuota()) {
			throw new BusinessException(BusinessErrorCode.QUOTA_PLATFORM_UNAUTHORIZED, "The platforme quota has been reached.");
		}
	}
}