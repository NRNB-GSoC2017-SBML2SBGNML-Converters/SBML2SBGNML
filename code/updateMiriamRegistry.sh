#!/bin/sh

wget --content-disposition http://www.ebi.ac.uk/miriam/main/export/xml/

TODAY=`date +%F`

mv IdentifiersOrg-Registry* miriam-${TODAY}.xml

cp miriam-${TODAY}.xml miriam.xml