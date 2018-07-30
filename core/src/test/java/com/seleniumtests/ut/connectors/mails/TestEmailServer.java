/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.connectors.mails;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.mails.EmailServer;
import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;
import com.seleniumtests.customexception.ConfigurationException;

public class TestEmailServer extends Object {

	
	
	@Test(groups={"ut"})
	public void testFromJson() {
		EmailServer server = EmailServer.fromJson("{'url': 'msg.company.com', 'type': 'EXCHANGE_EWS', 'domain': 'company'}");
		Assert.assertEquals(server.getUrl(), "msg.company.com");
		Assert.assertEquals(server.getType(), EmailServerTypes.EXCHANGE_EWS);
		Assert.assertEquals(server.getDomain(), "company");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testFromJson2() {
		EmailServer.fromJson("{'url': 'msg.company.com', 'type': 'EXCHANGE_EWS'}");
	}
	
	@Test(groups={"ut"})
	public void testToJson() {
		EmailServer server = new EmailServer("msg.company.com", EmailServerTypes.EXCHANGE, "company");
		Assert.assertEquals(server.toJson(), "{\"domain\":\"company\",\"type\":\"EXCHANGE\",\"url\":\"msg.company.com\"}");
	}
}
