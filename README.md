A Groovy friendly CSS Selector using
http://github.com/chrsan/css-selectors

###Using with HTTPBuilder

######Introduction

CSS Selector is used with HTTPBuilder by registering it as a parser for 'text/html' content type.

######Details
```groovy
import groovyx.net.http.CSSSelector
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.DOMParser
import org.xml.sax.InputSource

def http = new HTTPBuilder('http://www.google.com/');

http.parser.'text/html' = {resp ->
  DOMParser p = new DOMParser();
  def content = resp.getEntity().getContent()
  p.parse(new InputSource(content));
  return new CSSSelector(p.getDocument());
}

def html = http.get(path: 'search', query: [q: 'groovy'])

// print search result titles, may not work as Google search result page keeps changing
html.'ol li h3 a'.each{
  println it.text();
}

```
