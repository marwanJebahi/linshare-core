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
package org.linagora.linshare.core.batches.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.linagora.linshare.core.batches.WeeklyBatch;
import org.linagora.linshare.core.business.service.DomainDailyStatBusinessService;
import org.linagora.linshare.core.business.service.DomainWeeklyStatBusinessService;
import org.linagora.linshare.core.business.service.ThreadDailyStatBusinessService;
import org.linagora.linshare.core.business.service.ThreadWeeklyStatBusinessService;
import org.linagora.linshare.core.business.service.UserDailyStatBusinessService;
import org.linagora.linshare.core.business.service.UserWeeklyStatBusinessService;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.domain.entities.Thread;

public class WeeklyBatchImpl implements WeeklyBatch {

	private UserWeeklyStatBusinessService userWeeklyStatBusinessService;
	private ThreadWeeklyStatBusinessService threadWeeklyStatBusinessService;
	private DomainWeeklyStatBusinessService domainWeeklyStatBusinessService;

	private UserDailyStatBusinessService userDailyStatBusinessService;
	private ThreadDailyStatBusinessService threadDailyStatBusinessService;
	private DomainDailyStatBusinessService domainDailyStatBusinessService;

	public WeeklyBatchImpl(UserWeeklyStatBusinessService userWeeklyStatBusinessService, ThreadWeeklyStatBusinessService threadWeeklyStatBusinessService, DomainWeeklyStatBusinessService domainWeeklyStatBusinessService,
			UserDailyStatBusinessService userDailyStatBusinessService, ThreadDailyStatBusinessService threadDailyStatBusinessService, DomainDailyStatBusinessService domainDailyStatBusinessService) {
		this.userWeeklyStatBusinessService = userWeeklyStatBusinessService;
		this.threadWeeklyStatBusinessService = threadWeeklyStatBusinessService;
		this.domainWeeklyStatBusinessService = domainWeeklyStatBusinessService;
		this.userDailyStatBusinessService = userDailyStatBusinessService;
		this.threadDailyStatBusinessService = threadDailyStatBusinessService;
		this.domainDailyStatBusinessService = domainDailyStatBusinessService;
	}

	@Override
	public void executeBatch() {
		Date lastDayOfLastWeek = getLastDayOfLastWeek();
		Date firstDayOfLastWeek = getFirstDayOfLastWeek();
		List<Account> listUser = userDailyStatBusinessService.findAccountBetweenTwoDates(firstDayOfLastWeek, lastDayOfLastWeek);
		List<Account> listThread = threadDailyStatBusinessService.findAccountBetweenTwoDates(firstDayOfLastWeek, lastDayOfLastWeek);
		List<AbstractDomain> listDomain = domainDailyStatBusinessService.findDomainBetweenTwoDates(firstDayOfLastWeek, lastDayOfLastWeek);

		for (Account user : listUser) {
			userWeeklyStatBusinessService.create((User)user, firstDayOfLastWeek, lastDayOfLastWeek);
		}

		for (Account thread : listThread) {
			threadWeeklyStatBusinessService.create((Thread)thread, firstDayOfLastWeek, lastDayOfLastWeek);
		}

		for (AbstractDomain domain : listDomain) {
			domainWeeklyStatBusinessService.create(domain, firstDayOfLastWeek, lastDayOfLastWeek);
		}
	}

	private Date getLastDayOfLastWeek() {
		GregorianCalendar dateCalendar = new GregorianCalendar();
		Date firstDate = getFirstDayOfLastWeek();
		dateCalendar.setTime(firstDate);
		int today = dateCalendar.get(GregorianCalendar.DAY_OF_MONTH);
		dateCalendar.set(GregorianCalendar.DAY_OF_MONTH, today + 6);
		dateCalendar.set(GregorianCalendar.HOUR, 0);
		dateCalendar.set(GregorianCalendar.MINUTE, 0);
		dateCalendar.set(GregorianCalendar.SECOND, 0);
		return dateCalendar.getTime();
	}

	private Date getFirstDayOfLastWeek() {
		GregorianCalendar dateCalendar = new GregorianCalendar();
		dateCalendar.add(Calendar.DATE, -7);
		int firstDay = dateCalendar.getFirstDayOfWeek();
		if(firstDay == GregorianCalendar.SUNDAY)
			firstDay++;
		else{
			if(firstDay != GregorianCalendar.MONDAY)
				throw new BusinessException("the used local can not be supported");
		}
		dateCalendar.set(GregorianCalendar.DAY_OF_WEEK, firstDay);
		dateCalendar.set(GregorianCalendar.HOUR, 0);
		dateCalendar.set(GregorianCalendar.MINUTE, 0);
		dateCalendar.set(GregorianCalendar.SECOND, 0);
		return dateCalendar.getTime();
	}
}