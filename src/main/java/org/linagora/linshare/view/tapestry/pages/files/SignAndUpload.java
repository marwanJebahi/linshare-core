/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2016 LINAGORA
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
package org.linagora.linshare.view.tapestry.pages.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.linagora.linshare.core.domain.objects.FileInfo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.exception.TechnicalErrorCode;
import org.linagora.linshare.core.exception.TechnicalException;
import org.linagora.linshare.core.facade.FunctionalityFacade;
import org.linagora.linshare.view.tapestry.objects.CustomStreamResponse;
import org.linagora.linshare.view.tapestry.services.Templating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignAndUpload {

	private static final Logger logger = LoggerFactory.getLogger(SignAndUpload.class);


	@SessionState
	@Property
	private UserVo userVo;


	@Inject @Symbol("javawebstart.decrypt.url.suffixcodebase")
	private String suffixcodebase;

	@Inject
	@Path("context:templates/jws/SignAndUpload.jnlp")
	private Asset jwsTemplate;

	@Inject
	private FunctionalityFacade functionalityFacade;

	@Inject
	private Templating templating;

	@Property
	private String sessionId;

	public CustomStreamResponse onActivate() throws BusinessException {

		sessionId = functionalityFacade.getSessionId();
		try {
			String tplcontent = templating.readFullyTemplateContent(jwsTemplate.getResource().openStream());

			String linshareInfoUrl = functionalityFacade.getCustomNotificationURLInRootDomain();

			Map<String,String> templateParams=new HashMap<String, String>();
			//result codebase for JNLP is an url like http://localhost:8080/linshare/applet to download signature-client.jar
			StringBuffer jwsUrlToPut = new StringBuffer(linshareInfoUrl);
			StringBuffer serveurUrl = new StringBuffer(linshareInfoUrl);
			
			if(!linshareInfoUrl.endsWith("/")) {
				jwsUrlToPut.append('/');
				serveurUrl.append('/');
			}
			serveurUrl.append("webservice/rest/user/");
			templateParams.put("${serverURL}", serveurUrl.toString());
			jwsUrlToPut.append(suffixcodebase); //application jws directory: applet in this case
			if(suffixcodebase.endsWith("/")) jwsUrlToPut.deleteCharAt(jwsUrlToPut.length()-1);
			templateParams.put("${javawebstart.decrypt.url.codebase}", jwsUrlToPut.toString());
			templateParams.put("${sessionId}", sessionId);
			String jnlp = templating.getMessage(tplcontent, templateParams);

			byte[] send = jnlp.getBytes();
			long size = send.length;
			ByteArrayInputStream bi = new ByteArrayInputStream(send);

			return new CustomStreamResponse(new FileInfo("","SignAndUpload.jnlp","",size,"application/x-java-jnlp-file"),bi);

		} catch (IOException e) {
			logger.error("Bad jws template", e);
			throw new TechnicalException(TechnicalErrorCode.GENERIC,"Bad jws template",e);
		}
	}

}
