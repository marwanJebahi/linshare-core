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
package org.linagora.linshare.core.domain.entities;

import java.util.UUID;

public class MailAttachment {

	private long id;

	private String uuid;

	private Boolean enable;

	private Document document;

	private Boolean override;

	private Integer language;

	private String description;

	private String name;

	private MailConfig mailConfig;

	private AbstractDomain domain;

	private String alt;

	private String cid;

	public MailAttachment() {
		super();
	}

	public MailAttachment(Boolean enable, Document document, Boolean override, Integer language,
			String description, String name, MailConfig mailConfig, AbstractDomain domain, String cid, String alt) {
		super();
		this.uuid = UUID.randomUUID().toString();
		this.enable = enable;
		this.override = override;
		this.language = language;
		this.description = description;
		this.name = name;
		this.mailConfig = mailConfig;
		this.domain = domain;
		this.cid = cid;
		this.alt = alt;
		this.document = document;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Boolean getOverride() {
		return override;
	}

	public void setOverride(Boolean override) {
		this.override = override;
	}

	public Integer getLanguage() {
		return language;
	}

	public void setLanguage(Integer language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MailConfig getMailConfig() {
		return mailConfig;
	}

	public void setMailConfig(MailConfig mailConfig) {
		this.mailConfig = mailConfig;
	}

	public AbstractDomain getDomain() {
		return domain;
	}

	public void setDomain(AbstractDomain domain) {
		this.domain = domain;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	@Override
	public String toString() {
		return "MailAttachment [uuid=" + uuid + ", enable=" + enable + ", document=" + document + ", override="
				+ override + ", language=" + language + ", description=" + description + ", name=" + name
				+ ", mailConfig=" + mailConfig + ", domain=" + domain + ", alt=" + alt + ", cid=" + cid + "]";
	}
}
