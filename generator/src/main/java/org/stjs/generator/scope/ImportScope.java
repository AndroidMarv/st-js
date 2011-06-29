package org.stjs.generator.scope;

import org.stjs.generator.scope.NameType.IdentifierName;
import org.stjs.generator.scope.NameType.MethodName;

/**
 * This scope tries to solve the methods inside the static imports and the identifiers (that can be the name of a class)
 * in the imports, the current package and the default packages.
 * 
 * @author <a href='mailto:ax.craciun@gmail.com'>Alexandru Craciun</a>
 * 
 */
public class ImportScope extends NameScope {
	
	
	public ImportScope() {
		super(null);
	}

	@Override
	public QualifiedName<MethodName> resolveMethod(String name, NameScope currentScope) {
		if (getParent() != null) {
			return getParent().resolveMethod(name, currentScope);
		}
		return null;
	}

	@Override
	public QualifiedName<IdentifierName> resolveIdentifier(String name, NameScope currentScope) {
		if (getParent() != null) {
			return getParent().resolveIdentifier(name, currentScope);
		}
		return null;
	}

	@Override
	public String toString() {
		return "ImportScope [getChildren()=" + getChildren() + "]";
	}

}
