var Promise = require("bluebird");
var fs = require("fs")
var path = require("path")
const asr2js = require('android-string-resource/asr2js');
var json2csv = require('json2csv');
var fields = ['key'];

var root = path.join(__dirname+"/../")
var cfg=  require("./config/config")
var xmlList = cfg.get("strings");

var listRes=[];


Promise.map(xmlList, function (fileInfo) {
    var data=fs.readFileSync(root+fileInfo.path+"/strings.xml","utf-8");
    console.log(data);
    console.log("READ FILE SYNC END");
    fields.push(fileInfo.name);
    return new Promise(function (r, v) {
        asr2js(data, (err, res) => {
            // res is like js
            console.log(res)
            listRes.push({
                name:fileInfo.name,
                res:res
            });
            return r()

        });
    })

}, {concurrency: 1}).then(function (result) {
    console.log("then whileCb");
    outcsv();
    //whileCb();
    return result;
}).catch(function (e) {
    console.log(e);
});




function outcsv() {
    var myData = [];
    var keys = Object.keys(listRes[0].res);

        keys.forEach(function (t) {
            let obj={
                key:t,
            }
            listRes.forEach(function (res) {
                obj[res.name] = res.res[t]
            })
            myData.push(obj)
    })
    try {

        var result = json2csv({ data: myData, fields: fields });
        console.log(result);
        fs.writeFileSync(root+'strings.csv',result);
        console.log("writeFileSync "+root+'strings.csv');
    } catch (err) {
        // Errors are thrown for bad options, or if the data is empty and no fields are provided.
        // Be sure to provide fields if it is possible that your data array will be empty.
        console.error(err);
    }

}
