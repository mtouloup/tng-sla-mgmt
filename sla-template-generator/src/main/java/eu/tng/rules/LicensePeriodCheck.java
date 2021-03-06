/*
 * 
 *  Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 *  ALL RIGHTS RESERVED.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 *  nor the names of its contributors may be used to endorse or promote
 *  products derived from this software without specific prior written
 *  permission.
 *  
 *  This work has been performed in the framework of the SONATA project,
 *  funded by the European Commission under Grant number 671517 through
 *  the Horizon 2020 and 5G-PPP programmes. The authors would like to
 *  acknowledge the contributions of their colleagues of the SONATA
 *  partner consortium (www.sonata-nfv.eu).
 *  
 *  This work has been performed in the framework of the 5GTANGO project,
 *  funded by the European Commission under Grant number 761493 through
 *  the Horizon 2020 and 5G-PPP programmes. The authors would like to
 *  acknowledge the contributions of their colleagues of the 5GTANGO
 *  partner consortium (www.5gtango.eu).
 * 
 */

package eu.tng.rules;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import eu.tng.correlations.db_operations;

/**
 * Application Lifecycle Listener implementation class LicensePeriodCheck
 *
 */
public class LicensePeriodCheck implements ServletContextListener {

	static Logger logger = LogManager.getLogger();

	// LOGS VARIABLES
	String timestamps = "";
	String type = "";
	String operation = "";
	String message = "";
	String status = "";
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());

	/**
	 * @contextDestroyed
	 */
	public void contextDestroyed(ServletContextEvent event) {
		// logging
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestamps = timestamp.toString();
		String type = "I";
		String operation = "License Check Listener";
		String message = "[*] Listener License Check Stopped! - Restarting....";
		String status = "";
		logger.info(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		contextInitialized(event);
	}

	/**
	 * contextInitialized
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		// logging
		timestamp = new Timestamp(System.currentTimeMillis());
		timestamps = timestamp.toString();
		type = "I";
		operation = "License Check Listener";
		message = ("[*] License Check Listener started!!");
		status = "";
		logger.info(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		// run every 24h - 24*60*60*1000 add 24 hours delay between job executions.
		final long timeInterval = 24 * 60 * 60 * 1000;
		Runnable runnable = new Runnable() {

			public void run() {
				while (true) {
					
					// code for task to run ends here
					try {
						Thread.sleep(6000);
					} 
					catch (InterruptedException e) {
						// logging
						timestamp = new Timestamp(System.currentTimeMillis());
						timestamps = timestamp.toString();
						type = "I";
						operation = "License Check Listener";
						message = ("[*] Thread Error ==> " + e);
						status = "";
						logger.info(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type, timestamps, operation, message, status);
					}
					
					
					// code for task to run
					Date currentDate = new Date();
					Date exp_date = new Date();

					db_operations db = new db_operations();
					db_operations.connectPostgreSQL();
					db_operations.createTableLicensing();
					org.json.simple.JSONArray licenses = db_operations.getAllLicenses();
					db_operations.closePostgreSQL();

					if (licenses.size() == 0) {
						// logging
						timestamp = new Timestamp(System.currentTimeMillis());
						timestamps = timestamp.toString();
						type = "I";
						operation = "License Check Listener";
						message = ("[*] No licenses yet.");
						status = "";
						logger.info(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type, timestamps, operation, message, status);
						
					} else {
						for (int i = 0; i < licenses.size(); i++) {
							JSONObject license_item = (JSONObject) licenses.get(i);
							String license_exp_date_string = (String) ((JSONObject) license_item)
									.get("license_exp_date");

							if (license_exp_date_string != null || license_exp_date_string != "") {
								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
								String dateInString = license_exp_date_string;
								try {
									exp_date = formatter.parse(dateInString.replaceAll("Z$", "+0000"));
								} catch (ParseException e) {

									// logging
									timestamp = new Timestamp(System.currentTimeMillis());
									timestamps = timestamp.toString();
									type = "I";
									operation = "License Check Listener";
									message = ("[*] Error formating date: " + e.getMessage());
									status = "";
									logger.info(
											"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
											type, timestamps, operation, message, status);
								}

								String license_nsi_uuid = (String) ((JSONObject) license_item).get("nsi_uuid");

								if (currentDate.after(exp_date)) {
									db_operations.connectPostgreSQL();

									// logging
									timestamp = new Timestamp(System.currentTimeMillis());
									timestamps = timestamp.toString();
									type = "I";
									operation = "License Check Listener";
									message = ("[*] License expired: Current date after license expiration date");
									status = "";
									logger.info(
											"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
											type, timestamps, operation, message, status);

									// sde-activate the license
									db_operations.deactivateLicenseForNSI(license_nsi_uuid, "expired");									
									db_operations.closePostgreSQL();

									// send termination request for the service
									HttpClient httpClient = HttpClientBuilder.create().build();
									try {
										// HttpPost request = new
										// HttpPost("http://pre-int-sp-ath.5gtango.eu:32002/api/v3/requests");
										HttpPost request = new HttpPost(System.getenv("GATEKEEPER_URL") + "requests");
										StringEntity params = new StringEntity("{\"instance_uuid\":\""
												+ license_nsi_uuid + "\",\"request_type\":\"TERMINATE_SERVICE\"}");
										request.addHeader("content-type", "application/json");
										request.setEntity(params);
										httpClient.execute(request);

									} catch (Exception ex) {
										// logging
										timestamp = new Timestamp(System.currentTimeMillis());
										timestamps = timestamp.toString();
										type = "W";
										operation = "License Check Listener";
										message = ("[*] Error sending termination request: " + ex.getMessage());
										status = "";
										logger.warn(
												"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
												type, timestamps, operation, message, status);
									}

								} else {
									// logging
									timestamp = new Timestamp(System.currentTimeMillis());
									timestamps = timestamp.toString();
									type = "I";
									operation = "License Check Listener";
									message = ("[*] License not expired yet.");
									status = "";
									logger.info(
											"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
											type, timestamps, operation, message, status);
								}
							}

						}

					}

					// code for task to run ends here
					try {
						Thread.sleep(timeInterval);
					} 
					catch (InterruptedException e) {
						// logging
						timestamp = new Timestamp(System.currentTimeMillis());
						timestamps = timestamp.toString();
						type = "I";
						operation = "License Check Listener";
						message = ("[*] Thread Error ==> " + e);
						status = "";
						logger.info(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type, timestamps, operation, message, status);
					}
				}
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();

	}

}
