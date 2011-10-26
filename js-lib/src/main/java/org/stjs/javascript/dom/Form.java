/**
 *  Copyright 2011 Alexandru Craciun, Eyal Kaspi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"){throw new UnsupportedOperationException();}
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.stjs.javascript.dom;

public class Form extends Element {
	public String acceptCharset;
	public String action;
	public HTMLCollection<Input> elements;
	public String enctype;
	public int length;
	public String method;
	public String name;
	public String target;

	public void reset() {
		throw new UnsupportedOperationException();
	}

	public void submit() {
		throw new UnsupportedOperationException();
	}
}