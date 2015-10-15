package com.reprezen.swagedit.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.reprezen.swagedit.editor.SwaggerCompletionProposal;

public class SwaggerCompletionProposalTest {

	@Test
	public void test() {
		SwaggerCompletionProposal proposal = SwaggerCompletionProposal.create();

		assertNotNull(proposal);
		assertFalse(proposal.getProposals().isEmpty());
	}

}
