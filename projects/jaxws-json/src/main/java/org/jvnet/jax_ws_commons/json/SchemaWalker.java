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

    private final Set<XSComponent> visited = new HashSet<XSComponent>();

    public void annotation(final XSAnnotation ann) {
    }

    public void attGroupDecl(final XSAttGroupDecl decl) {
        if(!visited.add(decl))   return;
        for( final XSAttributeUse u : decl.getAttributeUses() )
            attributeUse(u);
    }

    public void attributeDecl(final XSAttributeDecl decl) {
        if(!visited.add(decl))   return;
        simpleType(decl.getType());
    }

    public void attributeUse(final XSAttributeUse use) {
        if(!visited.add(use))   return;
        attributeDecl(use.getDecl());
    }

    public void complexType(final XSComplexType type) {
        if(!visited.add(type))   return;
        type.getContentType().visit(this);
        for( final XSAttributeUse u : type.getAttributeUses() )
            attributeUse(u);
    }

    public void schema(final XSSchema schema) {
        if(!visited.add(schema))   return;
        for (final XSElementDecl e : schema.getElementDecls().values())
            elementDecl(e);
        for (final XSAttributeDecl a : schema.getAttributeDecls().values())
            attributeDecl(a);
        for (final XSAttGroupDecl a : schema.getAttGroupDecls().values())
            attGroupDecl(a);
        for (final XSModelGroupDecl m : schema.getModelGroupDecls().values())
            modelGroupDecl(m);
        for (final XSType t : schema.getTypes().values())
            t.visit(this);
        for (final XSNotation n : schema.getNotations().values())
            notation(n);
    }

    public void facet(final XSFacet facet) {
    }

    public void notation(final XSNotation notation) {
    }

    public void identityConstraint(final XSIdentityConstraint decl) {
    }

    public void xpath(final XSXPath xp) {
    }

    public void wildcard(final XSWildcard wc) {
    }

    public void modelGroupDecl(final XSModelGroupDecl decl) {
        if(!visited.add(decl))   return;
        modelGroup(decl.getModelGroup());
    }

    public void modelGroup(final XSModelGroup group) {
        if(!visited.add(group))   return;
        for (final XSParticle p : group.getChildren())
            particle(p);
    }

    public void elementDecl(final XSElementDecl decl) {
        if(!visited.add(decl))   return;
        decl.getType().visit(this);
    }

    public void simpleType(final XSSimpleType simpleType) {
        if(!visited.add(simpleType))   return;
        simpleType.getBaseType().visit(this);
    }

    public void particle(final XSParticle particle) {
        if(!visited.add(particle))   return;
        particle.getTerm().visit(this);
    }

    public void empty(final XSContentType empty) {
    }
}
