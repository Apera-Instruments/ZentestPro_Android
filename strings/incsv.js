var Promise = require("bluebird");
var fs = require("fs")
var path = require("path")
const asr2js = require('android-string-resource/asr2js');
const js2asr = require('android-string-resource/js2asr');
var json2csv = require('json2csv');
var fields = ['key'];

var root = path.join(__dirname+"/../")
var cfg=  require("./config/config")
var xmlList = cfg.get("strings");


var csv = require("fast-csv");
var fileCSV = root+'strings.csv';
var stream = fs.createReadStream(fileCSV);

var listRes = []
csv.fromStream(stream)
    .on("data", function (data) {
        console.log(data);
        listRes.push(data)
    })
    .on("end", function () {
        console.log("done");
        let fields =  listRes[0];
        var outObjects =[];
        for(let i=1;i<fields.length;i++){
            outObjects.push({
                name:fields[i],
                res:{}
            })
        }
        console.log(outObjects);
        for(let i=1;i<listRes.length;i++){
            let fields =  listRes[i];
            for(let fieldsIndex=1;fieldsIndex<fields.length;fieldsIndex++){
                outObjects[fieldsIndex-1].res[fields[0]] = fields[fieldsIndex] || "undefine";

            }
        }
        console.log(JSON.stringify(outObjects));

        outObjects.forEach(function (result) {
            js2asr(result.res, (err, res) => {
                // res is like xml
                console.log(res)
                var path;
                xmlList.forEach(function (file) {
                    if(file.name===result.name){
                        path = file.path;
                    }
                })
                path =root+path+"/strings.xml"
                fs.writeFileSync(path,res);
                console.log("writeFileSync "+path);
            });
        })



    });