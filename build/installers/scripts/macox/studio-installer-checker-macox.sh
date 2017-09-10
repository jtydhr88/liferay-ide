source ../config/env.properties 

#1 check pwd file does not exist

if [ ! -f "/private/tmp/LiferayWorkspace/pwd" ]; then
echo "Pwd file does not exist.(Successfully)"
else
echo "Pwd file exists.(Error)"
fi

#2 check ${system_temp_directory} does not exist

if [ ! -d "/private/tmp/LiferayWorkspace" ]; then
echo "LiferayWorkspace temp dir does not exist.(Successfully)"
else
echo "LiferayWorkspace temp dir exists.(Error)"
fi

#3 check jpm is installed, jpm/bin exists, .jpm exists

jpmVersion=`jpm version`
if [ "$jpmVersion" == "$exceptedJpmVersion" ];then
echo "Jpm $exceptedJpmVersion is installed correctly.(Successfully)"
else
echo "Jpm version is not $exceptedJpmVersion.(Error)"
fi

if [ -f "$HOME/Library/PackageManager/bin/jpm" ]; then
echo "Jpm file exists.(Successfully)"
else
echo "Jpm file does not exist.(Error)"
fi

if [ -d "$HOME/.jpm" ]; then
echo "Folder .jpm exists.(Successfully)"
else
echo "Folder .jpm does not exist.(Error)"
fi

#4 check blade is installed, jpm/blade exists

bladeVersion=`blade version`
if [ "$bladeVersion" == "$exceptedBladeVersion" ];then
echo "Blade $bladeVersion is installed correctly.(Successfully)"
else
echo "Blade version is not $exceptedBladeVersion.(Error)"
fi

if [ -f "$HOME/Library/PackageManager/bin/blade" ]; then
echo "Blade file exists.(Successfully)"
else
echo "Blade file does not exist.(Error)"
fi

#5 check token file generated

if [ -f "$HOME/.liferay/token" ]; then
echo "Token file generated correctly.(Successfully)"
else
echo "No token file.(Error)"
fi

#6 check bundle.url in gradle.properties

if [ `grep "$bundleName" $HOME/$liferayWsInStudioDir/gradle.properties` ];then 
echo "Found $bundleName in $liferayWsInStudioDir gradle.properties file.(Successfully)"
else
echo "Can't found $bundleName in $liferayWsInStudioDir gradle.properties file.(Error)"
fi

#7 check liferay workspace home dir

if [ -d "$HOME/$liferayWsDir" ]; then
echo "Liferay Workpsace exists.(Successfully)"
else
echo "Liferay Workspace does not exist.(Error)"
fi

#8 check Studio home dir

if [ -d "$HOME/$studioDir" ]; then
echo "Developer stuido dir exists.(Successfully)"
else
echo "Developer stuido dir does not exist.(Error)"
fi

#9 check plugins sdk in bundles folder

if [ -f "$HOME/.liferay/bundles/$pluginsSdkName" ]; then
echo "Plugins sdk exists.(Successfully)"
else
echo "Plugins sdk does not exist.(Error)"
fi

#10 check dxp tomcat in bundles folder
if [ -f "$HOME/.liferay/bundles/$dxpName" ]; then
echo "Dxp tomcat exists.(Successfully)"
else
echo "Dxp tomcat does not exist.(Error)"
fi
