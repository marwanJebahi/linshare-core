/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.DomainPermissionBusinessService;
import org.linagora.linshare.core.business.service.JwtLongTimeBusinessService;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.JwtToken;
import org.linagora.linshare.core.rac.JwtLongTimeResourceAccessControl;
import org.linagora.linshare.core.repository.AccountRepository;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.AuditLogEntryService;
import org.linagora.linshare.core.service.JwtLongTimeService;
import org.linagora.linshare.core.service.JwtService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.mongo.entities.JwtLongTime;
import org.linagora.linshare.mongo.entities.logs.AuditLogEntryUser;
import org.linagora.linshare.mongo.entities.logs.JwtLongTimeAuditLogEntry;

import io.jsonwebtoken.Clock;
import io.jsonwebtoken.impl.DefaultClock;

public class JwtLongTimeServiceImpl extends GenericServiceImpl<Account, JwtLongTime> implements JwtLongTimeService {

	protected Clock clock = DefaultClock.INSTANCE;

	protected String issuer;

	protected JwtService jwtService;

	protected JwtLongTimeBusinessService jwtLongTimeBusinessService;

	private DomainPermissionBusinessService permissionService;

	private AbstractDomainService abstractDomainService;

	private AccountRepository<Account> accountRepository;

	private LogEntryService logEntryService;

	private AuditLogEntryService auditLogEntryService;

	public JwtLongTimeServiceImpl(
			String issuer,
			JwtLongTimeBusinessService jwtLongTimeBusinessService,
			JwtService jwtService,
			JwtLongTimeResourceAccessControl rac,
			DomainPermissionBusinessService permissionSerivce,
			AbstractDomainService abstractDomainService,
			AccountRepository<Account> accountRepository,
			LogEntryService logEntryService,
			AuditLogEntryService auditLogEntryService) {
		super(rac);
		this.issuer = issuer;
		this.jwtLongTimeBusinessService = jwtLongTimeBusinessService;
		this.jwtService = jwtService;
		this.permissionService = permissionSerivce;
		this.abstractDomainService = abstractDomainService;
		this.accountRepository = accountRepository;
		this.logEntryService = logEntryService;
		this.auditLogEntryService = auditLogEntryService;
	}

	@Override
	public JwtLongTime create(Account authUser, Account actor, String label, String description) throws BusinessException {
		Validate.notNull(actor, "actor must be set");
		final Date creationDate = clock.now();
		final String tokenUuid = UUID.randomUUID().toString();
		if (!actor.isInternal()) {
			String message = "You can not generate JWT token for account which is not internal user.";
			throw new BusinessException(BusinessErrorCode.METHOD_NOT_ALLOWED, message);
		}
		JwtLongTime jwtLongTime = new JwtLongTime(tokenUuid, creationDate, issuer, label, description, actor.getLsUuid(),
				actor.getMail(), actor.getDomainId());
		String token = jwtService.generateToken(actor, tokenUuid, creationDate);
		jwtLongTimeBusinessService.create(jwtLongTime);
		AuditLogEntryUser createLog = new JwtLongTimeAuditLogEntry(authUser, actor, LogAction.CREATE,
				AuditLogEntryType.JWT_LONG_TIME_TOKEN, jwtLongTime);
		logEntryService.insert(createLog);
		jwtLongTime.setJwtToken(new JwtToken(token));
		return jwtLongTime;
	}

	@Override
	public List<JwtLongTime> findAllByActor(Account actor) throws BusinessException {
		Validate.notNull(actor);
		List<JwtLongTime> tokens = jwtLongTimeBusinessService.findAllByActor(actor);
		return tokens;
	}

	@Override
	public JwtLongTime delete(Account authUser, Account actor, JwtLongTime jwtLongTime) throws BusinessException {
		Validate.notNull(authUser, "AuthUser must be set.");
		Validate.notNull(actor, "Actor must be set");
		Validate.notNull(jwtLongTime, "Token uuid must be set");
		checkDeletePermission(authUser, actor, JwtLongTime.class, BusinessErrorCode.JWT_LONG_TIME_CAN_NOT_DELETE,
				jwtLongTime);
		AuditLogEntryUser createLog = new JwtLongTimeAuditLogEntry(authUser, actor, LogAction.DELETE,
				AuditLogEntryType.JWT_LONG_TIME_TOKEN, jwtLongTime);
		jwtLongTimeBusinessService.deleteToken(jwtLongTime);
		logEntryService.insert(createLog);
		return jwtLongTime;
	}

	@Override
	public JwtLongTime find(Account authUser, Account actor, String uuid) throws BusinessException {
		Validate.notNull(authUser, "AuthUser must be set.");
		Validate.notNull(actor, "Actor must be set");
		Validate.notEmpty(uuid, "Token uuid must be set");
		JwtLongTime found = jwtLongTimeBusinessService.find(uuid);
		if (found == null) {
			String message = "The requested token has not been found.";
			throw new BusinessException(BusinessErrorCode.JWT_LONG_TIME_NOT_FOUND, message);
		}
		checkReadPermission(authUser, actor, JwtLongTime.class, BusinessErrorCode.JWT_LONG_TIME_CAN_NOT_READ, found);
		return found;
	}

	@Override
	public List<JwtLongTime> findAllByDomain(Account authUser, AbstractDomain domain) throws BusinessException {
		Validate.notNull(domain, "domain must be set");
		if (!permissionService.isAdminforThisDomain(authUser, domain)) {
			throw new BusinessException(BusinessErrorCode.JWT_LONG_TIME_FORBIDDEN,
					"You are not allowed to use this domain");
		}
		return jwtLongTimeBusinessService.findAllByDomain(domain.getUuid());
	}

	@Override
	public JwtLongTime deleteByAdmin(Account authUser, JwtLongTime jwtLongTime) throws BusinessException {
		Validate.notNull(jwtLongTime, "jwtLongTime must be set");
		AbstractDomain domain = abstractDomainService.findById(jwtLongTime.getDomainUuid());
		if (!permissionService.isAdminforThisDomain(authUser, domain)) {
			throw new BusinessException(BusinessErrorCode.JWT_LONG_TIME_FORBIDDEN,
					"You are not allowed to use this domain");
		}
		JwtLongTime jwtToken = find(authUser, authUser, jwtLongTime.getUuid());
		Account actor = accountRepository.findByLsUuid(jwtToken.getActorUuid());
		jwtLongTimeBusinessService.deleteToken(jwtLongTime);
		AuditLogEntryUser createLog = new JwtLongTimeAuditLogEntry(authUser, actor, LogAction.DELETE,
				AuditLogEntryType.JWT_LONG_TIME_TOKEN, jwtLongTime);
		logEntryService.insert(createLog);
		return jwtToken;
	}

	@Override
	public Set<AuditLogEntryUser> findAllAudit(Account authUser, String domainUuid, List<LogAction> actions)
			throws BusinessException {
		if (authUser.hasAdminRole()) {
			Validate.notNull(domainUuid, "domainUuid must be set");
			AbstractDomain domain = abstractDomainService.findById(domainUuid);
			if(!permissionService.isAdminforThisDomain(authUser, domain)) {
				throw new BusinessException(BusinessErrorCode.JWT_LONG_TIME_FORBIDDEN,
						"You are not allowed to use this domain");
			}
		}
		return auditLogEntryService.findAllAudit(authUser, domainUuid, actions);
	}

}