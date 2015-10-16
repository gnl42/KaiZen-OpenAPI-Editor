package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerCompletionProposal
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull

class CompletionProposalTest {

	@Test
	def test() {
		val proposal = SwaggerCompletionProposal.create()

		assertNotNull(proposal)
		assertFalse(proposal.getProposals().isEmpty())
	}

}