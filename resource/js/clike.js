.CodeMirror {
  line-height: 1em;
  font-family: monospace;

  /* Necessary so the scrollbar can be absolutely positioned within the wrapper on Lion. */
  position: relative;
  /* This prevents unwanted scrollbars from showing up on the body and wrapper in IE. */
  overflow: hidden;
}

.CodeMirror-scroll {
  overflow: auto;
  height: 300px;
  /* This is needed to prevent an IE[67] bug where the scrolled content
     is visible outside of the scrolling box. */
  position: relative;
  outline: none;
}

/* Vertical scrollbar */
.CodeMirror-scrollbar {
  position: absolute;
  right: 0; top: 0;
  overflow-x: hidden;
  overflow-y: scroll;
  z-index: 5;
}
.CodeMirror-scrollbar-inner {
  /* This needs to have a nonzero width in order for the scrollbar to appear
     in Firefox and IE9. */
  width: 1px;
}
.CodeMirror-scrollbar.cm-sb-overlap {
  /* Ensure that the scrollbar appears in Lion, and that it overlaps the content
     rather than sitting to the right of it. */
  position: absolute;
  z-index: 1;
  float: none;
  right: 0;
  min-width: 12px;
}
.CodeMirror-scrollbar.cm-sb-nonoverlap {
  min-width: 12px;
}
.CodeMirror-scrollbar.cm-sb-ie7 {
  min-width: 18px;
}

.CodeMirror-gutter {
  position: absolute; left: 0; top: 0;
  z-index: 10;
  background-color: #f7f7f7;
  border-right: 1px solid #eee;
  min-width: 2em;
  height: 100%;
}
.CodeMirror-gutter-text {
  color: #aaa;
  text-align: right;
  padding: .4em .2em .4em .4em;
  white-space: pre !important;
  cursor: default;
}
.CodeMirror-lines {
  padding: .4em;
  white-space: pre;
  cursor: text;
}

.CodeMirror pre {
  -moz-border-radius: 0;
  -webkit-border-radius: 0;
  -o-border-radius: 0;
  border-radius: 0;
  border-width: 0; margin: 0; padding: 0; background: transparent;
  font-family: inherit;
  font-size: inherit;
  padding: 0; margin: 0;
  white-space: pre;
  word-wrap: normal;
  line-height: inherit;
  color: inherit;
  overflow: visible;
}

.CodeMirror-wrap pre {
  word-wrap: break-word;
  white-space: pre-wrap;
  word-break: normal;
}
.CodeMirror-wrap .CodeMirror-scroll {
  overflow-x: hidden;
}

.CodeMirror textarea {
  outline: none !important;
}

.CodeMirror pre.CodeMirror-cursor {
  z-index: 10;
  position: absolute;
  visibility: hidden;
  border-left: 1px solid black;
  border-right: none;
  width: 0;
}
.cm-keymap-fat-cursor pre.CodeMirror-cursor {
  width: auto;
  border: 0;
  background: transparent;
  background: rgba(0, 200, 0, .4);
  filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#6600c800, endColorstr=#4c00c800);
}
/* Kludge to turn off filter in ie9+, which also accepts rgba */
.cm-keymap-fat-cursor pre.CodeMirror-cursor:not(#nonsense_id) {
  filter: progid:DXImageTransform.Microsoft.gradient(enabled=false);
}
.CodeMirror pre.CodeMirror-cursor.CodeMirror-overwrite {}
.CodeMirror-focused pre.CodeMirror-cursor {
  visibility: visible;
}

div.CodeMirror-selected { background: #d9d9d9; }
.CodeMirror-focused div.CodeMirror-selected { background: #d7d4f0; }

.CodeMirror-searching {
  background: #ffa;
  background: rgba(255, 255, 0, .4);
}

/* Default theme */

.cm-s-default span.cm-keyword {color: #708;}
.cm-s-default span.cm-atom {color: #219;}
.cm-s-default span.cm-number {color: #164;}
.cm-s-default span.cm-def {color: #00f;}
.cm-s-default span.cm-variable {color: black;}
.cm-s-default span.cm-variable-2 {color: #05a;}
.cm-s-default span.cm-variable-3 {color: #085;}
.cm-s-default span.cm-property {color: black;}
.cm-s-default span.cm-operator {color: black;}
.cm-s-default span.cm-comment {color: #a50;}
.cm-s-default span.cm-string {color: #a11;}
.cm-s-default span.cm-string-2 {color: #f50;}
.cm-s-default span.cm-meta {color: #555;}
.cm-s-default span.cm-error {color: #f00;}
.cm-s-default span.cm-qualifier {color: #555;}
.cm-s-default span.cm-builtin {color: #30a;}
.cm-s-default span.cm-bracket {color: #997;}
.cm-s-default span.cm-tag {color: #170;}
.cm-s-default span.cm-attribute {color: #00c;}
.cm-s-default span.cm-header {color: blue;}
.cm-s-default span.cm-quote {color: #090;}
.cm-s-default span.cm-hr {color: #999;}
.cm-s-default span.cm-link {color: #00c;}

span.cm-header, span.cm-strong {font-weight: bold;}
span.cm-em {font-style: italic;}
span.cm-emstrong {font-style: italic; font-weight: bold;}
span.cm-link {text-decoration: underline;}

span.cm-invalidchar {color: #f00;}

div.CodeMirror span.CodeMirror-matchingbracket {color: #0f0;}
div.CodeMirror span.CodeMirror-nonmatchingbracket {color: #f22;}

@media print {

  /* Hide the cursor when printing */
  .CodeMirror pre.CodeMirror-cursor {
    visibility: hidden;
  }

}