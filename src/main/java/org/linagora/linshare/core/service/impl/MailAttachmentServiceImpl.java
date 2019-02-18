/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2019 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2019. Contribute to
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

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.MailAttachmentBusinessService;
import org.linagora.linshare.core.business.service.MailConfigBusinessService;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Document;
import org.linagora.linshare.core.domain.entities.MailAttachment;
import org.linagora.linshare.core.domain.entities.MailConfig;
import org.linagora.linshare.core.service.MailAttachmentService;

public class MailAttachmentServiceImpl implements MailAttachmentService{

	protected final MailAttachmentBusinessService attachmentBusinessService;

	protected final MailConfigBusinessService configService;

	public MailAttachmentServiceImpl(
			MailAttachmentBusinessService attachmentBusinessService,
			MailConfigBusinessService configService) {
		super();
		this.attachmentBusinessService = attachmentBusinessService;
		this.configService = configService;
	}

	@Override
	public MailAttachment create(Account authUser, boolean enable, String fileName, boolean override, String confUuid,
			String description, String alt, Document document, String cid, int language) {
		MailConfig config = configService.findByUuid(confUuid);
		Validate.notNull(config);
		Validate.notNull(authUser.getDomain());
		MailAttachment mailAttachment = new MailAttachment(enable, document, override, language, description, fileName,
				config, authUser.getDomain(), cid, alt);
		MailAttachment attachment = attachmentBusinessService.create(mailAttachment);
		return attachment;
	}
}