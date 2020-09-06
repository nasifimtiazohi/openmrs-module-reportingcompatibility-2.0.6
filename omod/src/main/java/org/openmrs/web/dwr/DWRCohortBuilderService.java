/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.cohort.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.cohort.CohortUtil;
import org.openmrs.module.reportingcompatibility.service.CohortService;
import org.openmrs.module.reportingcompatibility.service.ReportService;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.ReportConstants;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ReportObject;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.ReportingcompatibilityUtil;
import org.openmrs.web.controller.analysis.CohortBuilderController;

public class DWRCohortBuilderService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public Integer getResultCountForFilterId(Integer filterId) {
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		PatientFilter pf = rs.getPatientFilterById(filterId);
		if (pf == null)
			return null;
		Cohort everyone = Context.getService(ReportService.class).getAllPatients();
		Cohort filtered = (Cohort) pf.filter(everyone, null);
		return filtered.size();
	}
	
	private CohortSearchHistory getMySearchHistory() {
		return (CohortSearchHistory) CohortBuilderController.getVolatileUserData("CohortBuilderSearchHistory");
	}
	
	/**
	 * @param index
	 * @return the number of patients in the resulting PatientSet
	 */
	public Integer getResultCountForSearch(int index) {
		CohortSearchHistory history = getMySearchHistory();
		Cohort ps = history.getPatientSet(index, null);
		return ps.size();
	}
	
	public Cohort getResultForSearch(int index) {
		CohortSearchHistory history = getMySearchHistory();
		Cohort ps = history.getPatientSet(index, null);
		return ps;
	}
	
	public Cohort getResultCombineWithAnd() {
		CohortSearchHistory history = getMySearchHistory();
		Cohort ps = history.getPatientSetCombineWithAnd(new EvaluationContext());
		return ps;
	}
	
	public Cohort getResultCombineWithOr() {
		CohortSearchHistory history = getMySearchHistory();
		Cohort ps = history.getPatientSetCombineWithOr(new EvaluationContext());
		return ps;
	}
	
	public Cohort getLastResult() {
		CohortSearchHistory history = getMySearchHistory();
		if (history != null) {
			Cohort ps = history.getLastPatientSet(null);
			return ps;
		}
		return 
			new Cohort();
	}
	
	public List<ListItem> getSavedSearches(boolean includeParameterized) {
		List<ListItem> ret = new ArrayList<ListItem>();
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		List<AbstractReportObject> savedSearches = rs.getReportObjectsByType(
				ReportConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		for (ReportObject ps : savedSearches) {
			if (includeParameterized || ((PatientSearchReportObject) ps).getPatientSearch().getParameters().size() == 0) {
				ListItem li = new ListItem();
				li.setId(ps.getReportObjectId());
				li.setName(ps.getName());
				li.setDescription(ps.getDescription());
				ret.add(li);
			}
		}
		return ret;
	}
	
	public List<ListItem> getSavedFilters() {
		List<ListItem> ret = new ArrayList<ListItem>();
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		List<PatientFilter> savedFilters = rs.getAllPatientFilters();
		for (PatientFilter pf : savedFilters) {
			ListItem li = new ListItem();
			li.setId(pf.getReportObjectId());
			li.setName(pf.getName());
			li.setDescription(pf.getDescription());
			ret.add(li);
		}
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public List<ListItem> getSavedCohorts() {
		List<ListItem> ret = new ArrayList<ListItem>();
		List<org.openmrs.Cohort> cohorts = Context.getCohortService().getAllCohorts();
		for (org.openmrs.Cohort cht : cohorts) {
			ListItem li = new ListItem();
			Cohort c = ReportingcompatibilityUtil.convert(cht);
			li.setId(c.getCohortId());
			li.setName(c.getName());
			li.setDescription(c.getDescription());
			ret.add(li);
		}
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param filterId
	 * @return
	 */
	public String getFilterResultAsCommaSeparatedIds(Integer filterId) {
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		PatientFilter pf = rs.getPatientFilterById(filterId);
		if (pf == null)
			return "";
		else
			return Context.getService(ReportService.class).getAllPatients().getCommaSeparatedPatientIds();
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param cohortId
	 * @return
	 */
	public String getCohortAsCommaSeparatedIds(Integer cohortId) {
		Cohort c = ReportingcompatibilityUtil.convert(Context.getCohortService().getCohort(cohortId));
		if (c == null)
			return "";
		else
			return c.getCommaSeparatedPatientIds();
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public List<ListItem> getSearchHistories() {
		List<ListItem> ret = new ArrayList<ListItem>();
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		List<CohortSearchHistory> histories = rs.getAllSearchHistories();
		for (CohortSearchHistory h : histories) {
			ListItem li = new ListItem();
			li.setId(h.getReportObjectId());
			li.setName(h.getName());
			li.setDescription(h.getDescription());
			ret.add(li);
		}
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param name
	 * @param description
	 */
	public void saveSearchHistory(String name, String description) {
		CohortSearchHistory history = getMySearchHistory();
		if (history.getReportObjectId() != null)
			throw new RuntimeException("Re-saving search history Not Yet Implemented");
		history.setName(name);
		history.setDescription(description);
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		rs.saveSearchHistory(history);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param id
	 */
	public void loadSearchHistory(Integer id) {
		ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
		CohortBuilderController.setVolatileUserData("CohortBuilderSearchHistory", rs.getSearchHistory(id));
	}
	
	/**
	 * Saves an element from the search history as a PatientSearch
	 * 
	 * @param name The name to give the saved filter
	 * @param description The description to give the saved filter
	 * @param indexInHistory The index into the authenticated user's search history
	 */
	public Boolean saveHistoryElement(String name, String description, Integer indexInHistory) {
		CohortSearchHistory history = getMySearchHistory();
		try {
			PatientSearch ps = history.getSearchHistory().get(indexInHistory);
			if (ps == null)
				return false;
			// some searches depend on history, so we must detach them
			ps = ps.copyAndDetachFromHistory(history);
			PatientSearchReportObject ro = new PatientSearchReportObject();
			ro.setName(name);
			ro.setDescription(description);
			ro.setPatientSearch(ps);
			ReportObjectService rs = (ReportObjectService) Context.getService(ReportObjectService.class);
			rs.saveReportObject(ro);
			history.getSearchHistory().set(indexInHistory, PatientSearch.createSavedSearchReference(ro.getReportObjectId()));
			return true;
		}
		catch (Exception ex) {
			log.error("Exception", ex);
			return false;
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param name
	 * @param description
	 * @param commaSeparatedIds
	 */
	public void saveCohort(String name, String description, String commaSeparatedIds) {
		Set<Integer> ids = new HashSet<Integer>(OpenmrsUtil.delimitedStringToIntegerList(commaSeparatedIds, ","));
		org.openmrs.Cohort cohort = new org.openmrs.Cohort();
		cohort.setName(name);
		cohort.setDescription(description);
		cohort.setMemberIds(ids);
		Context.getCohortService().saveCohort(cohort);
	}
	
	/**
	 * This isn't really useful because most of the properties don't have DWR converters. I'm
	 * leaving it here in case I get to work on it later.
	 */
	public CohortSearchHistory getUserSearchHistory() {
		return getMySearchHistory();
	}
	
	/**
	 * Accepts an input cohortSpecification String, which represents a Cohort Definition to
	 * evaluate. Returns a Vector of Parameters that are required to evaluate the given
	 * CohortDefinition. Any parameter value that is an expression as determined by
	 * {@link EvaluationContext#isExpression(String)} will be returned. For example: For a
	 * cohortSpecification of [Male], an empty Vector is returned, as there are no parameters. For a
	 * cohortSpecification of [PregnantOnDate|effectiveDate=${?}], a Vector containing
	 * "effectiveDate" is returned
	 * 
	 * @param cohortSpecification - This input String represents the Cohort Definition to evaluate
	 * @return Vector<Parameter> containing all Parameters that need to be provided to evaluate the
	 *         input cohortSpecification
	 */
	public Vector<Parameter> getMissingParameters(String cohortSpecification) {
		Vector<Parameter> ret = new Vector<Parameter>();
		CohortDefinition def = CohortUtil.parse(cohortSpecification);
		for (Parameter p : def.getParameters()) {
			ret.add(p);
		}
		return ret;
	}
	
	/**
	 * Accepts an input cohortSpecification String, which represents a Cohort Definition to
	 * evaluate, along with a Map<Parameter, Object> which provides values for each missing
	 * parameter
	 * 
	 * @param cohortSpecification - This input String represents the Cohort Definition to evaluate
	 * @return Cohort - The Cohort of patients that are returned
	 */
	public Cohort evaluateCohortDefinition(String cohortSpecification, Map<Parameter, Object> parameterValues) {
		CohortDefinition def = CohortUtil.parse(cohortSpecification);
		EvaluationContext evalContext = new EvaluationContext();
		if (parameterValues != null) {
			for (Parameter p : parameterValues.keySet()) {
				Object v = parameterValues.get(p);
				evalContext.addParameterValue(p, v);
			}
		}
		CohortService svc = Context.getService(CohortService.class);
		return svc.evaluate(def, evalContext);
	}
}
