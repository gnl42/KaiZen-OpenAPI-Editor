package com.reprezen.swagedit.tests.utils

import com.fasterxml.jackson.core.JsonPointer

class PointerHelpers {
	def ptr(String s) { JsonPointer.compile(s) }
}