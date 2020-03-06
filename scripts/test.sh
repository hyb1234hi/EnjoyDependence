#!/usr/bin/env bash

function errorExit(){
    exit 1
}

version="5.40.2"
lastCommitId="57789339"
./gradlew updateLif -Plbv="${version}" -Pglc="${lastCommitId}"
./gradlew loadLifByType -PlifType="lbv"

    lbvPath=`pwd`/.lifTemp
    if [ ! -f "${lbvPath}" ]; then
        echo "缓存文件不存在，版本号获取不到"
        errorExit
    fi
    lbvVersion=$(cat "$lbvPath")

    if [[ $lbvVersion !=  *"-"* ]]; then
        version=$lbvVersion
        echo $version
    else
        lbvMajorVersion=$(echo ${lbvVersion%"-"*})
        echo "lbvMajorVersion: ${lbvMajorVersion}"
        lbvMinorVersion=$(echo ${lbvVersion#*"-"})
        echo "lbvMinorVersion: ${lbvMinorVersion}"
        if [[ ${lbvMajorVersion} == ${version} ]]; then
            version="$lbvMajorVersion-$(($lbvMinorVersion+1))"
            echo "$version"
        else
            echo "你当前的操作是违规的，请先尝试自动发布后，再补上车！！！如有疑问请联系发版人"
            errorExit
        fi
    fi

    rm -f $lbvPath
