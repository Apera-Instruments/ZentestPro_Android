//const android-string-resource = require('android-string-resource');
//const asr2js = require('android-string-resource/asr2js');
const xml = `<resources>
  <string name="key1">Hello</string>
  <string name="key2">An application to manipulate and process asr documents</string>
  <string name="key.nested">asr Data Manager</string>
</resources>`;

const js = {
  "key1": "Hello",
  "key2": "An application to manipulate and process asr documents",
  "key.nested": "asr Data Manager"
};

const asr2js = require('android-string-resource/asr2js');
asr2js(xml, (err, res) => {
  // res is like js
console.log(res)
});

const js2asr = require('android-string-resource/js2asr');
js2asr(js, (err, res) => {
  // res is like xml
    console.log(res)
});

/*

const  parser = require('res2json')
//let resources = parser.parse(project_root, project_module, locale)
//console.log(resources)
/!*
{ app_name: 'Application name',
  activities: 'Acitivities',
  activity: 'Activity',
  new_activity: 'New Activity',
  save: 'Save',
  field: 'Field',
  labour: 'Labour',
  at: 'at',
  action_settings: 'Settings',
  write_date: '%d de %s, %d',
  add_product: 'Add Product',
  delete: 'Delete',
  months:
   [ 'January',
     'February',
     'March',
     'April',
     'May',
     'June',
     'July',
     'August',
     'September',
     'October',
     'November',
     'December' ] }
*!/
// or directly using the path to the strings.xml
resources = parser.parseFile("../app/src/main/res/values/strings.xml")
console.log(resources)*/

//https://www.npmjs.com/package/json2csv
var json2csv = require('json2csv');
var fields = ['field1', 'field2', 'field3'];
var myData={"field2":"llll"}
try {
    var result = json2csv({ data: myData, fields: fields });
    console.log(result);
} catch (err) {
    // Errors are thrown for bad options, or if the data is empty and no fields are provided.
    // Be sure to provide fields if it is possible that your data array will be empty.
    console.error(err);
}
var csv = require("fast-csv");

var CSV_STRING = 'a,b\n' +
    'a1,b1\n' +
    'a2,b2\n';

csv
    .fromString(CSV_STRING, {headers: true})
    .on("data", function(data){
        console.log(data);
    })
    .on("end", function(){
        console.log("done");
    });

/*

var stream = fs.createReadStream("my.csv");

csv
    .fromStream(stream)
    .on("data", function(data){
        console.log(data);
    })
    .on("end", function(){
        console.log("done");
    });*/
