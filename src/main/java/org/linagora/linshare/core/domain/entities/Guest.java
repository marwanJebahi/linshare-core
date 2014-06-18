/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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

import java.util.Date;

import org.linagora.linshare.core.domain.constants.AccountType;
import org.linagora.linshare.webservice.dto.UserDto;

/** Guest is a user that is not registered in LDAP server.
 */
public class Guest extends User {

	private boolean restricted;

	private String comment;

	private Date expirationDate;
	

	/** Default constructor for hibernate. */
    @SuppressWarnings("unused")
	private Guest() {
        super();
    }

	public Guest(String firstName, String lastName, String mail, String password, Boolean canUpload, String comment) {
        super(firstName, lastName, mail);
        this.canUpload = canUpload;
        this.password = password;
        this.comment = comment;
        this.restricted = false;
        this.canCreateGuest = false;
    }
	
	public Guest(String firstName, String lastName, String mail) {
        super(firstName, lastName, mail);
        this.restricted = false;
        this.comment = "";
        this.canCreateGuest = false;
    }

	public Guest(UserDto guestDto) {
		super(guestDto.getFirstName(), guestDto.getLastName(), guestDto.getMail());
		this.restricted = guestDto.isRestricted();
		this.comment = guestDto.getComment();
		this.owner = new Internal(guestDto.getOwner());
		this.expirationDate = guestDto.getExpirationDate();
		this.canUpload = guestDto.getCanUpload();
		this.canCreateGuest = false;
	}

	@Override
	public AccountType getAccountType() {
		return AccountType.GUEST;
	}
    
	@Override
	public String getAccountReprentation() {
		return this.firstName + " " + this.lastName + "(" + lsUuid + ")";
	}

	public void setComment(String value) {
		this.comment = value;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setRestricted(boolean value) {
		this.restricted = value;
	}
	
	public boolean isRestricted() {
		return restricted;
	}

	public void setExpirationDate(Date value) {
		this.expirationDate = value;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}
	
}
