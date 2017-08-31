import resolve from 'rollup-plugin-node-resolve';
import commonjs from 'rollup-plugin-commonjs';
import json from 'rollup-plugin-json';
import builtins from 'rollup-plugin-node-builtins';

export default {
    input: 'chrome-run-qunit.src.js',
    output: {
        file: 'chrome-run-qunit.js',
        format: 'cjs'
    },
    plugins: [ resolve(),commonjs(),json() ],
    name: 'chromeRunQunit'
};
