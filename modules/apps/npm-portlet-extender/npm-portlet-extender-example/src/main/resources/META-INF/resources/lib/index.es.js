import isArray from 'isarray';

export default function(params) {
    var out = document.getElementById(params.portletElementId);
    
    var t = isArray([]);

    out.innerHTML = 'Portlet Namespace: ' + params.portletNamespace +
    '<br/>Context Path: ' + params.contextPath +
    '<br/>Portlet Element Id: ' + params.portletElementId +
    '<br/>Calling isArray([]) returns ' + t;
}
