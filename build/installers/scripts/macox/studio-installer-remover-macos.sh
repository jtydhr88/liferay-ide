source env-mac.properties 

#1 remove blade cache
rm -rf ~/.blade

#2 remove jpm cache 
rm -rf ~/.jpm

#3 remove token file
rm -rf ~/.liferay/token

#4 remove bundles
rm -rf ~/.liferay/bundles

#5 remove liferay workspce
rm -rf $liferayWsInStudioDir

#6 remove liferay developer stuido
rm -rf $studioDir

#7 eject liferay workspace
hdiutil eject "/Volumes/Liferay Workspace"

#8 eject liferay developer studio
hdiutil eject "/Volumes/Liferay Developer Studio"
