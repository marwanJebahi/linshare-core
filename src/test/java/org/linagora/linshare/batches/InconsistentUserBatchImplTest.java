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

package org.linagora.linshare.batches;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linagora.linshare.core.batches.GenericBatch;
import org.linagora.linshare.core.batches.InconsistentUserBatch;
import org.linagora.linshare.core.domain.constants.LinShareTestConstants;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Internal;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.job.quartz.BatchRunContext;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.runner.BatchRunner;
import org.linagora.linshare.core.service.InconsistentUserService;
import org.linagora.linshare.server.embedded.ldap.LdapServerRule;
import org.linagora.linshare.service.LoadingServiceTestDatas;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@ExtendWith(SpringExtension.class)
@ExtendWith(LdapServerRule.class)
@Sql({"/import-tests-inconsistent.sql"})
@Transactional
@ContextConfiguration(locations = { "classpath:springContext-datasource.xml", 
		"classpath:springContext-dao.xml",
		"classpath:springContext-ldap.xml", 
		"classpath:springContext-fongo.xml",
		"classpath:springContext-storage-jcloud.xml",
		"classpath:springContext-repository.xml",
		"classpath:springContext-business-service.xml",
		"classpath:springContext-rac.xml",
		"classpath:springContext-service-miscellaneous.xml",
		"classpath:springContext-service.xml",
		"classpath:springContext-batches.xml",
		"classpath:springContext-test.xml" })
public class InconsistentUserBatchImplTest {

	private static Logger logger = LoggerFactory.getLogger(UploadRequestNewBatchImplTest.class);

	@Autowired
	private BatchRunner batchRunner;

	@Qualifier("inconsistentUserBatch")
	@Autowired
	private InconsistentUserBatch inconsistentUserBatch;

	@Qualifier("userRepository")
	@Autowired
	private UserRepository<User> userRepository;

	@Autowired
	private InconsistentUserService inService;

	private LoadingServiceTestDatas datas;

	private Account root;

	public InconsistentUserBatchImplTest() {
		super();
	}

	@BeforeEach
	public void setUp() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_SETUP);
		datas = new LoadingServiceTestDatas(userRepository);
		datas.loadUsers();
		root = datas.getRoot();
		logger.debug(LinShareTestConstants.END_SETUP);
	}

	@AfterEach
	public void tearDown() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_TEARDOWN);
		logger.debug(LinShareTestConstants.END_TEARDOWN);
	}

	@Test
	public void testLaunching() throws JobExecutionException {
		logger.debug(LinShareTestConstants.BEGIN_TEST);
		List<GenericBatch> batches = Lists.newArrayList();
		batches.add(inconsistentUserBatch);
		Assertions.assertTrue(batchRunner.execute(batches),"At least one batch failed.");
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void testInconsistentBatch() {
		BatchRunContext batchRunContext = new BatchRunContext();
		logger.debug(LinShareTestConstants.BEGIN_TEST);
		List<String> list = inconsistentUserBatch.getAll(batchRunContext);
		int i = 0;
		for (String s : list) {
			inconsistentUserBatch.execute(batchRunContext, s, list.size(), i);
		}
		List<Internal> l = inService.findAllInconsistent((User)root);
		Assertions.assertEquals(l.size(), 5);
		logger.debug(LinShareTestConstants.END_TEST);
	}
}
