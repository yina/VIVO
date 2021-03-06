/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vivo.auth.policy;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.specialrelationships.RelationshipChecker;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractPropertyStatementAction;

/**
 * Does the requested action involve a change to a Grant that the self-editor is
 * authorized to modify?
 */
public class GrantChecker extends RelationshipChecker {
	private static final String NS_CORE = "http://vivoweb.org/ontology/core#";
	private static final String URI_GRANT_TYPE = NS_CORE + "Grant";
	private static final String URI_RELATED_ROLE_PROPERTY = NS_CORE
			+ "relatedRole";
	private static final String URI_PRINCIPAL_INVESTIGATOR_OF_PROPERTY = NS_CORE
			+ "principalInvestigatorRoleOf";
	private static final String URI_CO_PRINCIPAL_INVESTIGATOR_OF_PROPERTY = NS_CORE
			+ "co-PrincipalInvestigatorRoleOf";

	private final String[] resourceUris;

	public GrantChecker(AbstractPropertyStatementAction action) {
		super(action.getOntModel());
		this.resourceUris = action.getResourceUris();
	}

	/**
	 * A self-editor is authorized to add, edit, or delete a statement if the
	 * subject or object refers to a Grant, and if the self-editor:
	 * 
	 * 1) is a Principal Investigator (PI) of that Grant, or
	 * 
	 * 2) is a co-Principal Investigator (co-PI) of that Grant
	 */
	public PolicyDecision isAuthorized(List<String> userUris) {
		for (String resourceUri : resourceUris) {
			if (isGrant(resourceUri)) {
				if (anyUrisInCommon(userUris,
						getUrisOfPrincipalInvestigators(resourceUri))) {
					return authorizedPI(resourceUri);
				}
				if (anyUrisInCommon(userUris,
						getUrisOfCoPrincipalInvestigators(resourceUri))) {
					return authorizedCoPI(resourceUri);
				}
			}
		}
		return null;
	}

	private boolean isGrant(String resourceUri) {
		return isResourceOfType(resourceUri, URI_GRANT_TYPE);
	}

	private List<String> getUrisOfPrincipalInvestigators(String resourceUri) {
		return getObjectsOfLinkedProperty(resourceUri,
				URI_RELATED_ROLE_PROPERTY,
				URI_PRINCIPAL_INVESTIGATOR_OF_PROPERTY);
	}

	private List<String> getUrisOfCoPrincipalInvestigators(String resourceUri) {
		return getObjectsOfLinkedProperty(resourceUri,
				URI_RELATED_ROLE_PROPERTY,
				URI_CO_PRINCIPAL_INVESTIGATOR_OF_PROPERTY);
	}

	private PolicyDecision authorizedPI(String resourceUri) {
		return authorizedDecision("User is Principal Investigator of "
				+ resourceUri);
	}

	private PolicyDecision authorizedCoPI(String resourceUri) {
		return authorizedDecision("User is Co-Principal Investigator of "
				+ resourceUri);
	}

}
