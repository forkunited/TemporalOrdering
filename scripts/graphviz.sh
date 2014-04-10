#!/bin/bash
tmpname=${1:-tmp}
here=$(dirname $0)
cat | python $here/graphviz.py > ${tmpname}.dot
dot -Tpdf < ${tmpname}.dot > ${tmpname}.pdf

echo "Output: ${tmpname}.pdf"

# if [ `uname` = Darwin ]; then
#   open ${tmpname}.pdf
# fi
