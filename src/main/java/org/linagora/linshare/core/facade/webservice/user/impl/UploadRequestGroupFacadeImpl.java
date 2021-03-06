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

package org.linagora.linshare.core.facade.webservice.user.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.UploadRequestStatus;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.domain.entities.UploadRequestGroup;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.UploadRequestCreationDto;
import org.linagora.linshare.core.facade.webservice.common.dto.UploadRequestDto;
import org.linagora.linshare.core.facade.webservice.common.dto.UploadRequestGroupDto;
import org.linagora.linshare.core.facade.webservice.uploadrequest.dto.ContactDto;
import org.linagora.linshare.core.facade.webservice.user.UploadRequestGroupFacade;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.AuditLogEntryService;
import org.linagora.linshare.core.service.UploadRequestGroupService;
import org.linagora.linshare.core.service.UploadRequestService;
import org.linagora.linshare.mongo.entities.logs.AuditLogEntryUser;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class UploadRequestGroupFacadeImpl extends GenericFacadeImpl implements UploadRequestGroupFacade {

	private final UploadRequestGroupService uploadRequestGroupService;

	private final AuditLogEntryService auditLogEntryService;

	private final UploadRequestService uploadRequestService;

	public UploadRequestGroupFacadeImpl(AccountService accountService,
			final UploadRequestGroupService uploadRequestGroupService,
			final AuditLogEntryService auditLogEntryService,
			final UploadRequestService uploadRequestService) {
		super(accountService);
		this.uploadRequestGroupService = uploadRequestGroupService;
		this.auditLogEntryService = auditLogEntryService;
		this.uploadRequestService = uploadRequestService;
	}

	@Override
	public List<UploadRequestGroupDto> findAll(String actorUuid, List<UploadRequestStatus> status) throws BusinessException {
		Account authUser = checkAuthentication();
		Account actor = getActor(authUser, actorUuid);
		List<UploadRequestGroup> list = uploadRequestGroupService.findAll(authUser, actor, status);
		return ImmutableList.copyOf(Lists.transform(list, UploadRequestGroupDto.toDto()));
	}

	@Override
	public UploadRequestGroupDto find(String actorUuid, String uuid) throws BusinessException {
		Validate.notEmpty(uuid, "Upload request uuid must be set");
		Account authUser = checkAuthentication();
		Account actor = getActor(authUser, actorUuid);
		UploadRequestGroup group = uploadRequestGroupService.find(authUser, actor, uuid);
		return new UploadRequestGroupDto(group);
	}

	@Override
	public UploadRequestGroupDto create(String actorUuid, UploadRequestCreationDto uploadRequesCreationtDto,
			Boolean groupMode) throws BusinessException {
		Validate.notNull(uploadRequesCreationtDto, "Upload request must be set.");
		Validate.notEmpty(uploadRequesCreationtDto.getLabel(), "Upload request label must be set.");
		Validate.notEmpty(uploadRequesCreationtDto.getContactList(), "ContactList must be set");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequest req = uploadRequesCreationtDto.toObject();
		List<Contact> contacts = Lists.newArrayList();
		for (String mail : uploadRequesCreationtDto.getContactList()) {
			contacts.add(new Contact(mail));
		}
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.create(authUser, actor, req, contacts,
				uploadRequesCreationtDto.getLabel(), uploadRequesCreationtDto.getBody(), groupMode);
		return new UploadRequestGroupDto(uploadRequestGroup);
	}

	@Override
	public UploadRequestGroupDto updateStatus(String actorUuid, String requestGroupUuid, UploadRequestStatus status, boolean copy) throws BusinessException {
		Validate.notEmpty(requestGroupUuid, "Upload request group uuid must be set.");
		Validate.notNull(status, "Status must be set.");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.updateStatus(authUser, actor, requestGroupUuid, status, copy);
		return new UploadRequestGroupDto(uploadRequestGroup);
	}

	public UploadRequestGroupDto update(String actorUuid, UploadRequestGroupDto uploadRequestGroupDto, String uuid) {
		Validate.notNull(uploadRequestGroupDto, "Upload request group must be set.");
		if (!Strings.isNullOrEmpty(uuid)) {
			uploadRequestGroupDto.setUuid(uuid);
		}
		Validate.notEmpty(uploadRequestGroupDto.getUuid(), "Upload request group uuid must be set.");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupDto.toObject();
		return new UploadRequestGroupDto(uploadRequestGroupService.update(authUser, actor, uploadRequestGroup));
	}

	@Override
	public UploadRequestGroupDto addRecipients(String actorUuid, String groupUuid, List<ContactDto> recipientEmail) {
		Validate.notEmpty(groupUuid, "Upload request group must be set.");
		Validate.notEmpty(recipientEmail, "Upload request contact must be set.");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.find(authUser, actor, groupUuid);
		uploadRequestGroup = uploadRequestGroupService.addNewRecipients(authUser, actor, uploadRequestGroup, recipientEmail);
		return new UploadRequestGroupDto(uploadRequestGroup);
	}

	@Override
	public Set<AuditLogEntryUser> findAll(String actorUuid, String groupUuid, boolean detail, boolean entriesLogsOnly,
			List<LogAction> actions, List<AuditLogEntryType> types) {
		Validate.notEmpty(groupUuid, "Upload request group uuid must be set");
		Account authUser = checkAuthentication();
		User actor = (User) getActor(authUser, null);
		return auditLogEntryService.findAll(authUser, actor, groupUuid, detail, entriesLogsOnly, actions, types);
	}

	@Override
	public List<UploadRequestDto> findAllUploadRequests(String actorUuid, String groupUuid, List<UploadRequestStatus> status) {
		Validate.notEmpty(groupUuid, "Upload request group Uuid must be set");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.find(authUser, actor, groupUuid);
		List<UploadRequest> requests = uploadRequestService.findAll(authUser, actor, uploadRequestGroup, status);
		return ImmutableList.copyOf(Lists.transform(requests, UploadRequestDto.toDto(false)));
	}
}
