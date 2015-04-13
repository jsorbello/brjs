/**
* @module br/services/html/BRHtmlResourceService
*/

var br = require('br/Core');
var File = require('br/core/File');
var HtmlResourceService = require('br/services/HtmlResourceService');
var i18n = require('br/I18n');

/**
 * @class
 * @alias module:br/services/html/BRHtmlResourceService
 * @implements module:br/services/HtmlResourceService
 *
 * @classdesc
 * Provides access to HTML templates loaded via the HTML bundler.
 * This is the default HtmlResourceService in BladeRunnerJS
 *
 * @param {String} url A URL to load HTML from.
 */
function BRHtmlResourceService(url) {
	loadHtml(url || require('service!br.app-meta-service').getVersionedBundlePath("html/bundle.html"));
}

br.implement(BRHtmlResourceService, HtmlResourceService);

BRHtmlResourceService.prototype.getTemplateFragment = function(templateId) {
	var template = document.getElementById(templateId);
	return (template) ? document.importNode(template.content, true) : null;
};

BRHtmlResourceService.prototype.getTemplateElement = function(templateId) {
	var templateFragment = this.getTemplateFragment(templateId);
	
	if(!templateFragment) {
		return null
	}
	else {
		var templateNodes = nonEmptyNodes(templateFragment.childNodes);
		
		if(templateNodes.length != 1) throw new RangeError("The '" + templateId +
			"' template contained more than one root node -- use getTemplateFragment() instead.");
		
		return templateNodes[0];
	}
};

BRHtmlResourceService.prototype.getHTMLTemplate = function(templateId) {
	if(window.console) console.warn('getHTMLTemplate() is now deprecated -- please use getTemplateElement() instead.');
	return this.getTemplateElement(templateId);
};

function loadHtml(url) {
	var templateElems = document.createElement('div');
	document.querySelector('head').appendChild(templateElems); // TODO: move to bottom of method?

	var rawHtml = File.readFileSync(url);
	var translatedHtml = i18n.getTranslator().translate(rawHtml, "html");
	templateElems.innerHTML = sanitizeHtml(translatedHtml);

	fixNestedAutoWrappedTemplates();
	shimTemplates();
	fixAutoWrappedTemplates();
}

function nonEmptyNodes(childNodes) {
	var nonEmptyNodes = [];
	
	for(var i = 0, l = childNodes.length; i < l; ++i) {
		var childNode = childNodes[i];
		
		if((childNode.nodeType == document.ELEMENT_NODE) ||
			((childNode.nodeType == document.TEXT_NODE) && (childNode.textContent.trim() != ''))) {
			nonEmptyNodes.push(childNode);
		}
	}
	
	return nonEmptyNodes;
}

function shimTemplate(templateElem) {
	if(!templateElem.content) {
		templateElem.content = document.createDocumentFragment();
	}
}

function shimTemplates() {
	if(!('content' in document.createElement('template'))) {
		var templateElems = document.getElementsByTagName('template');

		for(var i = 0, l = templateElems.length; i < l; ++i) {
			var templateElem = templateElems[i];
			var templateContent = document.createDocumentFragment();

			while(templateElem.childNodes[0]) {
				templateContent.appendChild(templateElem.childNodes[0]);
			}

			templateElem.content = templateContent;
		}
	}
}

function fixNestedAutoWrappedTemplates() {
	var templateElems = document.querySelectorAll('template[data-auto-wrapped] template');

	for(var i = 0, l = templateElems.length; i < l; ++i) {
		var templateElem = templateElems[i];
		var parentTemplate = templateElem.parentNode;
		parentTemplate.parentNode.insertBefore(templateElem, parentTemplate.nextSibling);
	}
}

function fixAutoWrappedTemplates() {
	var templateElems = document.getElementsByTagName('template');

	for(var i = 0, l = templateElems.length; i < l; ++i) {
		var templateElem = templateElems[i];
		
		if(templateElem.hasAttribute('data-auto-wrapped')) {
			templateElem.removeAttribute('data-auto-wrapped');
			var templateNodes = nonEmptyNodes(templateElem.content.childNodes);
			
			for(var ni = 0, nl = templateNodes.length; ni < nl; ++ni) {
				var templateNode = templateNodes[ni];
				
				if(ni > 0) {
					if(templateNode.nodeName == 'TEMPLATE') {
						// Note: on browser's with native template support, this code is used instead
						// fixNestedAutoWrappedTemplates()
						templateElem.parentNode.insertBefore(templateNode, templateElem.nextSibling);
					}
					else {
						var newTemplateElem = document.createElement('template');
						shimTemplate(newTemplateElem);
						newTemplateElem.id = templateNode.id;
						newTemplateElem.content.appendChild(templateNode);
						templateElem.parentNode.insertBefore(newTemplateElem, templateElem.nextSibling);
						
						if(templateNode.hasAttribute('id')) {
							templateNode.removeAttribute('id');
						}
					}
				}
				else {
					if(templateNode.hasAttribute('id')) {
						templateNode.removeAttribute('id');
					}
				}
			}
		}
	}
}

// TODO: delete this method once we get to 2016
function sanitizeHtml(html) {
	function replacer(str, p1) {
		return '<div' + p1;
	}

	// IE and old Firefox's don't allow assigning text with script tag in it to innerHTML.
	if (html.match(/<script(.*)type=\"text\/html\"/)) {
	 	html = html.replace(/<script(.*)type=\"text\/html\"/g, replacer).replace(/<\/script>/g, '</div>');
	}

	return html;
}

module.exports = BRHtmlResourceService;
