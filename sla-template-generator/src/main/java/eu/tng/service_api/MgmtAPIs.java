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

package eu.tng.service_api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.tng.correlations.cust_sla_corr;
import eu.tng.correlations.db_operations;
import eu.tng.correlations.ns_template_corr;

@Path("/mgmt")
@Consumes(MediaType.APPLICATION_JSON)
public class MgmtAPIs {

	final static Logger logger = LogManager.getLogger();

	/**
	 * Get all ns-templates correlations
	 */
	@Path("/services/templates/")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTemplateNsCorrelations() {

		ResponseBuilder apiresponse = null;
		db_operations dbo = new db_operations();
		db_operations.connectPostgreSQL();
		dbo.createTableNSTemplate();
		JSONObject correlations = dbo.selectAllRecords("ns_template");

		db_operations.closePostgreSQL();
		
		apiresponse = Response.ok(correlations.toJSONString());
		apiresponse.header("Content-Length", correlations.toJSONString().length());

		// logging
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestamps = timestamp.toString();
		String type = "I";
		String operation = "Get all ns- templates correlations";
		String message = ("[*] Success! ns- templates correlations received");
		String status = "200";
		logger.info(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		return apiresponse.status(200).build();
	}

	/**
	 * Get all ns with associated sla template
	 */
	@Path("/services/templates/true")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNSwithTemplate() {
		ResponseBuilder apiresponse = null;
		ns_template_corr nstemplcorr = new ns_template_corr();
		JSONArray correlatedNS = nstemplcorr.nsWithTemplate();

		apiresponse = Response.ok((Object) correlatedNS);
		apiresponse.header("Content-Length", correlatedNS.toString().length());
		return apiresponse.status(200).build();
	}

	/**
	 * Get all ns with associated sla template
	 */
	@Path("/services/templates/false")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNSwithoutTemplate() {
		ResponseBuilder apiresponse = null;
		ns_template_corr nstemplcorr = new ns_template_corr();
		ArrayList<String> correlatedNS = nstemplcorr.nsWithoutTemplate();

		apiresponse = Response.ok((Object) correlatedNS);
		apiresponse.header("Content-Length", correlatedNS.toString().length());
		return apiresponse.status(200).entity(correlatedNS).build();
	}

	/**
	 * api call in order to get a predifined list with Service Guarantees
	 */
	@SuppressWarnings("unchecked")
	@GET
	//@Path("/guaranteesList")
	@Path("/guaranteesList/{ns_name}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getGuarantees(@PathParam("ns_name") String ns_name) {
	//public Response getGuarantees() {
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		
		JSONObject returnedSLOs = new JSONObject();
		JSONArray returned_guaranteeTerms = new JSONArray();

	
		ResponseBuilder apiresponse = null;
		try {
			File testf = new File(this.getClass().getResource("/slos_list_Y2.json").toURI());
			jsonObject = (JSONObject) parser.parse(new FileReader(testf));

			JSONArray guaranteeTerms = (JSONArray) jsonObject.get("guaranteeTerms");

			/**
			 * Select what slos to return basd on the NS that was selected
			 * 
			 */
			if (ns_name.matches("(.*)communication(.*)")) {
			
				for (int i = 0; i < guaranteeTerms.size(); i++) {
					JSONObject curr_gurantee = (JSONObject) guaranteeTerms.get(i);
					String guaranteeID = (String) curr_gurantee.get("guaranteeID");

					if (guaranteeID.equals("g1") || guaranteeID.equals("g2") || guaranteeID.equals("g3")
							|| guaranteeID.equals("g4") || guaranteeID.equals("g5") || guaranteeID.equals("g6")) {
						returned_guaranteeTerms.add(curr_gurantee);
					}
				}
				returnedSLOs.put("guaranteeTerms", returned_guaranteeTerms);


			}
			else if (ns_name.matches("(.*)mediapilot(.*)")) {
						
				for (int i = 0; i < guaranteeTerms.size(); i++) {
					JSONObject curr_gurantee = (JSONObject) guaranteeTerms.get(i);
					String guaranteeID = (String) curr_gurantee.get("guaranteeID");

					if (guaranteeID.equals("g1") || guaranteeID.equals("g2") || guaranteeID.equals("g3")
							|| guaranteeID.equals("g7") || guaranteeID.equals("g8") || guaranteeID.equals("g9")) {
						returned_guaranteeTerms.add(curr_gurantee);
					}
				}
				returnedSLOs.put("guaranteeTerms", returned_guaranteeTerms);

			}
			else {
				returnedSLOs = jsonObject;
			}
			
			apiresponse = Response.ok(returnedSLOs.toString());
			apiresponse.header("Content-Length", returnedSLOs.toString().length());
			
//			apiresponse = Response.ok(jsonObject.toString());
//			apiresponse.header("Content-Length", jsonObject.toString().length());

			// logging
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestamps = timestamp.toString();
			String type = "I";
			String operation = "Get Guarantee List";
			String message = ("[*] Success. Guarantees list received");
			String status = "200";
			logger.info(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			return apiresponse.status(200).build();

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			error.put("ERROR: ", "Guarantees List Not Found");
			apiresponse = Response.ok((Object) error);
			apiresponse.header("Content-Length", error.toJSONString().length());

			// logging
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestamps = timestamp.toString();
			String type = "W";
			String operation = "Get Guarantee List";
			String message = ("[*] Error. Guarantees list not found!");
			String status = "404";
			logger.warn(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			return apiresponse.status(404).build();

		}
	}

	/**
	 * Get all ns with associated sla agreement
	 */
	@Path("/services/agreements/true")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNSwithAgreement() {

		ResponseBuilder apiresponse = null;
		cust_sla_corr custslacorr = new cust_sla_corr();
		JSONArray correlatedNS = custslacorr.nsWithAgreement();

		apiresponse = Response.ok((Object) correlatedNS);
		apiresponse.header("Content-Length", correlatedNS.toString().length());
		return apiresponse.status(200).build();

	}

	/**
	 * Get all ns with associated sla agreement
	 */
	@Path("/services/agreements/false")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNSwithoutAgreement() {

		ResponseBuilder apiresponse = null;

		cust_sla_corr custslacorr = new cust_sla_corr();
		ArrayList<String> correlatedNS = custslacorr.nsWithoutAgreement();

		apiresponse = Response.ok((Object) correlatedNS);
		apiresponse.header("Content-Length", correlatedNS.toString().length());
		return apiresponse.status(200).build();

	}

	/**
	 * delete cust-ns-sla correlation based on sla uuid
	 */

	@Path("/agreements/{sla_uuid}")
	@Produces(MediaType.TEXT_PLAIN)
	@DELETE
	public Response deletecCustSlaCorrelation(@PathParam("sla_uuid") String sla_uuid) {
		ResponseBuilder apiresponse = null;

		db_operations db = new db_operations();
		db_operations.connectPostgreSQL();
		boolean delete = db.deleteRecord("cust_sla", sla_uuid);
		db_operations.closePostgreSQL();

		if (delete == true) {
			String response = "Agreement deleted Succesfully";
			apiresponse = Response.ok((response));
			apiresponse.header("Content-Length", response.length());
			return apiresponse.status(200).build();
		} else {
			String response = "Agreement was not deleted. sla_uuid Not Found";
			apiresponse = Response.ok((response));
			apiresponse.header("Content-Length", response.length());
			return apiresponse.status(404).build();
		}

	}

	/**
	 * Get QoS flavour names for a specific nsd
	 */
	@SuppressWarnings("unchecked")
	@Path("/deploymentflavours/{nsd_uuid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNsDeploymentFlavours(@PathParam("nsd_uuid") String nsd_uuid) {
		JSONParser parser = new JSONParser();
		ResponseBuilder apiresponse = null;

		try {
			String url = System.getenv("CATALOGUES_URL") + "network-services/" + nsd_uuid;
			// String url
			// ="http://pre-int-sp-ath.5gtango.eu:4011/catalogues/api/v2/network-services/"+nsd_uuid;
			URL object = new URL(url);

			HttpURLConnection con = (HttpURLConnection) object.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestMethod("GET");

			con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject nsd_JsonObject = (JSONObject) parser.parse(response.toString());

			// fetch the nsd and get list with deployment flavours names
			JSONObject nsd = (JSONObject) nsd_JsonObject.get("nsd");
			JSONArray flavour_names = new JSONArray();

			try {
				JSONArray deployment_flavours = (JSONArray) nsd.get("deployment_flavours");
				for (int i = 0; i < deployment_flavours.size(); i++) {
					JSONObject deployment_flavour_item = (JSONObject) deployment_flavours.get(i);
					String f_name = (String) ((JSONObject) deployment_flavour_item).get("name");
					flavour_names.add(f_name);
				}

				apiresponse = Response.ok(flavour_names);
				apiresponse.header("Content-Length", flavour_names.toJSONString().length());

			} catch (Exception e) {
				apiresponse = Response.ok(flavour_names);
				apiresponse.header("Content-Length", flavour_names.toJSONString().length());
			}

			// logging
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestamps = timestamp.toString();
			String type = "I";
			String operation = "Get Flavour Names List";
			String message = ("[*] Success. Deployment flavours received");
			String status = "200";
			logger.info(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			return apiresponse.status(200).build();

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			error.put("ERROR: ", " NSD Not Found");
			apiresponse = Response.ok((Object) error);
			apiresponse.header("Content-Length", error.toJSONString().length());

			// logging
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestamps = timestamp.toString();
			String operation = "Get NSD from catalogur";
			String type = "E";
			String message = "[*] Error: NSD with uuid=" + nsd_uuid + " NOT Found";
			String status = String.valueOf(404);
			logger.error(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			return apiresponse.status(404).build();
		}

		// try {
		// // get example nsd
		// File nsdf = new
		// File(this.getClass().getResource("/multi-flavour-nsd.json").toURI());
		// jsonObject = (JSONObject) parser.parse(new FileReader(nsdf));
		// System.out.println(jsonObject.toJSONString().length());
		//
		// // fetch the nsd and get list with deployment flavours names
		// JSONObject nsd = (JSONObject) jsonObject.get("nsd");
		// JSONArray deployment_flavours = (JSONArray) nsd.get("deployment_flavours");
		// JSONArray flavour_names = new JSONArray();
		// for (int i = 0; i < deployment_flavours.size(); i++) {
		// JSONObject deployment_flavour_item = (JSONObject) deployment_flavours.get(i);
		// String f_name = (String) ((JSONObject) deployment_flavour_item).get("name");
		// flavour_names.add(f_name);
		// }
		// System.out.println("[*] Deployment flavour name ==> " +
		// flavour_names.toString());
		//
		// apiresponse = Response.ok(flavour_names);
		// apiresponse.header("Content-Length", flavour_names.toJSONString().length());
		//
		// // logging
		// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		// String timestamps = timestamp.toString();
		// String type = "I";
		// String operation = "Get Flavour Names List";
		// String message = ("[*] Success. Deployment flavours received");
		// String status = "200";
		// logger.info(
		// "{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
		// type, timestamps, operation, message, status);
		//
		// return apiresponse.status(200).build();
		//
		// } catch (Exception e) {
		// JSONObject error = new JSONObject();
		// error.put("ERROR: ", "NSD File Not Found");
		// apiresponse = Response.ok((Object) error);
		// apiresponse.header("Content-Length", error.toJSONString().length());
		//
		// // logging
		// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		// String timestamps = timestamp.toString();
		// String type = "W";
		// String operation = "Get Guarantee List";
		// String message = ("[*] Error. NSD file not found!");
		// String status = "404";
		// logger.warn(
		// "{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
		// type, timestamps, operation, message, status);
		//
		// return apiresponse.status(404).build();
		//
		// }

	}

	/**
	 * Get QoS flavour names for a specific nsd
	 */
	@Path("/deploymentflavours/{nsd_uuid}/{sla_uuid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response GetSelectedFlavour(@PathParam("nsd_uuid") String nsd_uuid, @PathParam("sla_uuid") String sla_uuid) {

		ResponseBuilder apiresponse = null;

		new db_operations();
		db_operations.connectPostgreSQL();
		JSONObject dflavour_info = db_operations.getSpecificFlavour(nsd_uuid, sla_uuid);
		db_operations.closePostgreSQL();

		JSONObject response = dflavour_info;
		apiresponse = Response.ok((response));
		apiresponse.header("Content-Length", response.toJSONString().length());
		return apiresponse.status(200).build();

	}

}
