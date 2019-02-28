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
package org.linagora.linshare.core.business.service.impl;

import java.io.File;

import org.linagora.linshare.core.business.service.MailAttachmentBusinessService;
import org.linagora.linshare.core.business.service.ThumbnailGeneratorBusinessService;
import org.linagora.linshare.core.dao.FileDataStore;
import org.linagora.linshare.core.dao.MimeTypeMagicNumberDao;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Document;
import org.linagora.linshare.core.domain.entities.MailAttachment;
import org.linagora.linshare.core.domain.entities.MailConfig;
import org.linagora.linshare.core.repository.DocumentRepository;
import org.linagora.linshare.core.repository.MailAttachmentRepository;
import org.linagora.linshare.core.service.TimeStampingService;
import org.linagora.linshare.core.service.impl.AbstractDocumentBusinessServiceImpl;

public class MailAttachmentBusinessServiceImpl extends AbstractDocumentBusinessServiceImpl implements MailAttachmentBusinessService{

	protected final MailAttachmentRepository attachmentRepository;

	private final MimeTypeMagicNumberDao mimeTypeIdentifier;

	public MailAttachmentBusinessServiceImpl(
			FileDataStore fileDataStore,
			TimeStampingService timeStampingService,
			DocumentRepository documentRepository,
			ThumbnailGeneratorBusinessService thumbnailGeneratorBusinessService,
			boolean deduplication,
			MailAttachmentRepository attachmentRepository,
			MimeTypeMagicNumberDao mimeTypeIdentifier) {
		super(fileDataStore, timeStampingService, documentRepository,
				thumbnailGeneratorBusinessService, deduplication);
		this.attachmentRepository = attachmentRepository;
		this.mimeTypeIdentifier = mimeTypeIdentifier;
	}

	@Override
	public MailAttachment create(Account authUser, boolean enable, String fileName, boolean override,
			MailConfig mailConfig, String description, String alt, String cid, int language, File tempFile,
			String metaData) {
		String mimeType = mimeTypeIdentifier.getMimeType(tempFile);
		Document document = createDocument(authUser, tempFile, tempFile.length(), fileName, null, mimeType);
		MailAttachment mailAttachment = new MailAttachment(enable, document, override, language, description, fileName,
				mailConfig, authUser.getDomain(), cid, alt);
		return attachmentRepository.create(mailAttachment);
	}
}
