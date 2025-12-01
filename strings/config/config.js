const cfg = {
    db_name: "test",
    db_user: "test",
    db_password: "test",
    db_host: "127.0.0.1",
    strings:[
        {
            name: "English",
            path: 'ui/src/main/res/values'
        },
        {
            name: "中文",
            path: 'ui/src/main/res/values-zh-rCN'
        }
    ]
};



module.exports = {
    get: function (name) {
        return cfg[name];
    }


}