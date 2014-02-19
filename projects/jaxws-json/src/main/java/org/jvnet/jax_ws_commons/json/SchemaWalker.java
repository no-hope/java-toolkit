package org.jvnet.jax_ws_commons.json;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Visits the schema components.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SchemaWalker implements XSVisitor {

    private final Set<XSComponent> visited = new HashSet<>();

    @Override
    public void annotation(final XSAnnotation ann) {
    }

    @Override
    public void attGroupDecl(final XSAttGroupDecl decl) {
        if (!visited.add(decl)) {
            return;
        }
        for (final XSAttributeUse u : decl.getAttributeUses()) {
            attributeUse(u);
        }
    }

    @Override
    public void attributeDecl(final XSAttributeDecl decl) {
        if (!visited.add(decl)) {
            return;
        }
        simpleType(decl.getType());
    }

    @Override
    public void attributeUse(final XSAttributeUse use) {
        if (!visited.add(use)) {
            return;
        }
        attributeDecl(use.getDecl());
    }

    @Override
    public void complexType(final XSComplexType type) {
        if (!visited.add(type)) {
            return;
        }
        type.getContentType().visit(this);
        for (final XSAttributeUse u : type.getAttributeUses()) {
            attributeUse(u);
        }
    }

    @Override
    public void schema(final XSSchema schema) {
        if (!visited.add(schema)) {
            return;
        }
        for (final XSElementDecl e : schema.getElementDecls().values()) {
            elementDecl(e);
        }
        for (final XSAttributeDecl a : schema.getAttributeDecls().values()) {
            attributeDecl(a);
        }
        for (final XSAttGroupDecl a : schema.getAttGroupDecls().values()) {
            attGroupDecl(a);
        }
        for (final XSModelGroupDecl m : schema.getModelGroupDecls().values()) {
            modelGroupDecl(m);
        }
        for (final XSType t : schema.getTypes().values()) {
            t.visit(this);
        }
        for (final XSNotation n : schema.getNotations().values()) {
            notation(n);
        }
    }

    @Override
    public void facet(final XSFacet facet) {
    }

    @Override
    public void notation(final XSNotation notation) {
    }

    @Override
    public void identityConstraint(final XSIdentityConstraint decl) {
    }

    @Override
    public void xpath(final XSXPath xp) {
    }

    @Override
    public void wildcard(final XSWildcard wc) {
    }

    @Override
    public void modelGroupDecl(final XSModelGroupDecl decl) {
        if (!visited.add(decl)) {
            return;
        }
        modelGroup(decl.getModelGroup());
    }

    @Override
    public void modelGroup(final XSModelGroup group) {
        if (!visited.add(group)) {
            return;
        }
        for (final XSParticle p : group.getChildren()) {
            particle(p);
        }
    }

    @Override
    public void elementDecl(final XSElementDecl decl) {
        if (!visited.add(decl)) {
            return;
        }
        decl.getType().visit(this);
    }

    @Override
    public void simpleType(final XSSimpleType simpleType) {
        if (!visited.add(simpleType)) {
            return;
        }
        simpleType.getBaseType().visit(this);
    }

    @Override
    public void particle(final XSParticle particle) {
        if (!visited.add(particle)) {
            return;
        }
        particle.getTerm().visit(this);
    }

    @Override
    public void empty(final XSContentType empty) {
    }
}
