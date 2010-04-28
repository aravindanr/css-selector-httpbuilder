package groovyx.net.http;
/**
 Copyright 2010 Aravindan Ramkumar

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 */

import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import org.w3c.dom.Node;
import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

import java.util.*;

/**
 * Groovy facade for DOMNodeSelector. An instance of this class is Groovy friendly and is expected to be used in Groovy.
 * It operates on a set of org.w3c.dom.Node(s). All the methods create a new instance of this class to hold the results
 * except the <code>filter</code> method, which modifies the set of Node(s) in an instance.
 * <p/>
 * This class is inspired by the infamous jQuery object.
 * <p/>
 * Author: Aravindan Ramkumar
 */
public class CSSSelector extends GroovyObjectSupport {

    private Set<Node> nodes = new LinkedHashSet<Node>();

    /**
     * Creates an instance that operates on this Node.
     *
     * @param node The Node to operate on.
     */
    public CSSSelector(Node node) {
        this.nodes.add(node);
    }

    /**
     * Creates an instance that operates on this set of Node(s).
     *
     * @param nodes The Node to operate on.
     */
    public CSSSelector(Set<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * Queries the Node(s) in context on and return the results wrapped in a new CSSSelector.
     *
     * @param selector The CSS Selector to query for.
     * @return The CSS Selector object containing the result Node(s).
     */
    public CSSSelector query(String selector) {
        return new CSSSelector(_query(selector));
    }

    /**
     * @return String containing text value of Node(s).
     */
    public String text() {
        StringBuilder builder = new StringBuilder();
        for (Node node : this.nodes) {
            builder.append(node.getTextContent());
        }

        return builder.toString();
    }

    /**
     * The value of the attribute for the first Node in context.
     *
     * @param attrName The name of the attribute to retrieve.
     * @return Value of the attribute or null if the attribute is nt present.
     */
    public String attr(String attrName) {
        String attr = null;
        if (!this.nodes.isEmpty()) {
            Node node = this.nodes.iterator().next().getAttributes().getNamedItem(attrName);
            if (node != null) {
                attr = node.getTextContent();
            }
        }
        return attr;
    }

    /**
     * @return An immutable set containing the Node(s) in context.
     */
    public Set<Node> nodes() {
        return Collections.unmodifiableSet(this.nodes);
    }

    /**
     * Filters the Node(s) in context using the specified selector.
     *
     * @param selector The selector to filter.
     * @return A reference to this instance.
     */
    public CSSSelector filter(String selector) {
        this.nodes = _query(selector);
        return this;
    }

    /**
     * @return The size of Node(s)
     */
    public int size() {
        return this.nodes.size();
    }

    /**
     * Groovy support method.
     *
     * @return Iterator for the CSSSelector wrapping each Node.
     */
    public Iterator iterator() {
        return toSelectors().iterator();
    }

    /**
     * Groovy support method. Gets the Node wrapped in a CSSSelector at the specified index.
     * Negative indexes not supported.
     *
     * @param index The index to get.
     * @return The CSSSelector object wrapping the Node at this index.
     */
    public Object getAt(final int index) {
        if (index < 0 || index >= size()) throw new ArrayIndexOutOfBoundsException(index);

        Iterator itr = iterator();
        Object returnObj = null;
        for (int i = 0; i < size(); i++) {
            returnObj = itr.next();
            if (i == index) break;
        }

        return returnObj;
    }

    /**
     * Groovy Support Method. Gets the Node(s) wrapped in a CSSSelector at the specified range.
     * Negative ranges and reverse ranges not supported.
     *
     * @param range The range to look for.
     * @return The List of CSSSelector objects wrapping the Node(s) at the specfied range.
     */
    public Object getAt(final IntRange range) {
        final int from = range.getFromInt();
        final int to = range.getToInt();

        if (range.isReverse()) {
            throw new GroovyRuntimeException("Reverse ranges not supported, range supplied is [" + to + ".." + from + "]");
        } else if (from < 0 || to < 0) {
            throw new GroovyRuntimeException("Negative range indexes not supported, range supplied is [" + from + ".." + to + "]");
        } else {
            return new ArrayList<CSSSelector>(toSelectors()).subList(from, to);
        }
    }

    /**
     * Groovy Support Method. If the property begins with '@', then its considered as an attribute, else its considered
     * as a selector.
     *
     * @param property attribute name or selector.
     * @return Attribute value if property is an attribute name, CSSSelector list otherwise
     */
    @Override
    public Object getProperty(String property) {
        Object value;
        if (property.startsWith("@")) {
            value = attr(property.substring(1));
        } else {
            value = query(property);
        }

        return value;
    }

    /**
     * Uses DOMNodeSelector to query the Node(s) in context.
     *
     * @param selector The selector to query.
     * @return The Set of Node(s) resulting from the query
     */
    private Set<Node> _query(String selector) {
        Set<Node> results = new LinkedHashSet<Node>();

        for (Node node : this.nodes) {
            try {
                results.addAll(new DOMNodeSelector(node).querySelectorAll(selector));
            } catch (NodeSelectorException e) {
                throw new GroovyRuntimeException(e);
            }
        }
        return results;
    }

    /**
     * Converts a set of Node(s) to a Set of CSSSelector(s).
     *
     * @return The Set of CSSSelector(s).
     */
    private Set<CSSSelector> toSelectors() {
        Set<CSSSelector> selectorResults = new LinkedHashSet<CSSSelector>();
        for (Node node : this.nodes) {
            selectorResults.add(new CSSSelector(node));
        }
        return selectorResults;
    }

    /**
     * @param other The Object to match.
     * @return true if the object is not null and is an instance of CSSSelector and contains the same set of Node(s).
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        CSSSelector that = (CSSSelector) other;

        return this.nodes.equals(that.nodes);
    }

    /**
     * @return hash code of the Node(s) set.
     */
    @Override
    public int hashCode() {
        return this.nodes.hashCode();
    }

    /**
     * @return The text content of the Node(s) in context.
     */
    @Override
    public String toString() {
        return text();
    }
}
