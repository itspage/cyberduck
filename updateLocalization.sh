usage ( ) {
	echo "Usage: udpateLocalization.sh <language>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       as the base language, English is assumed"
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1;

for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    # First update the nib file with the modifications in the .strings file. The .strings file is always assumed to be newer - this means that all modifications should not be made in the .nib file directly but in the .strings file instead.
	./updateNibFromStrings.sh $language $nibfile
	# Update the localized .strings file with new strings added to to the base (English) version.
	./updateStringsFromNib.sh $language $nibfile
#	./updateNibFromStrings.sh $language $nibfile
done

exit 0

