/*
 *  Copyright (C) 2010-2013 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.rest.test.property;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import org.structr.rest.common.StructrRestTest;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Christian Morgner
 */
public class StringPropertyRestTest extends StructrRestTest {
	
	public void testBasics() {
		
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.body(" { 'stringProperty' : 'This is a test!' } ")
		.expect()
			.statusCode(201)
		.when()
			.post("/test_threes")
			.getHeader("Location");
		
		
		
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(200))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(201))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(400))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(422))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(500))
		.expect()
			.statusCode(200)
			.body("result[0].stringProperty", equalTo("This is a test!"))
		.when()
			.get("/test_threes");
		
		
		
	}
	
	public void testSearch() {

		RestAssured.given().contentType("application/json; charset=UTF-8").body(" { 'stringProperty' : 'test1' } ").expect().statusCode(201).when().post("/test_threes");
		RestAssured.given().contentType("application/json; charset=UTF-8").body(" { 'stringProperty' : 'test2' } ").expect().statusCode(201).when().post("/test_threes");
		RestAssured.given().contentType("application/json; charset=UTF-8").body(" { 'stringProperty' : 'test3' } ").expect().statusCode(201).when().post("/test_threes");
		
		// test for three elements
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(200))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(201))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(400))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(422))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(500))
		.expect()
			.statusCode(200)
			.body("result_count", equalTo(3))
		.when()
			.get("/test_threes");
		
		// test strict search
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(200))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(201))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(400))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(422))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(500))
		.expect()
			.statusCode(200)
			.body("result[0].stringProperty", equalTo("test2"))
		.when()
			.get("/test_threes?stringProperty=test2");
		
		
		// test loose search
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(200))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(201))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(400))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(422))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(500))
		.expect()
			.statusCode(200)
			.body("result_count", equalTo(3))
		.when()
			.get("/test_threes?stringProperty=test&loose=1");
		
		
		// test range query
		RestAssured.given()
			.contentType("application/json; charset=UTF-8")
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(200))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(201))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(400))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(422))
			.filter(ResponseLoggingFilter.logResponseIfStatusCodeIs(500))
		.expect()
			.statusCode(200)
			.body("result_count", equalTo(2))
		.when()
			.get("/test_threes?stringProperty=[test1 TO test2]");
		
		
	}
}
