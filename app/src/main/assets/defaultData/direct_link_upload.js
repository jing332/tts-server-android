let DirectUploadJS = {
    "橘途网盘 (永久有效)": function(config) {
        let resp = upload('http://v2.jt12.de/up-v2.php', config)
        let str = resp.body().string()
        let result = JSON.parse(str)
        if (result['code'] !== 0) {
            throw "error: " + result['msg']
        }

        return result['msg']
    },

    "喵公子 (有效期2天)": function(config) {
        let url = 'https://sy.mgz6.cc/shuyuan'
        let resp = upload(url, config)
        let result = JSON.parse(resp.body().string())
        if (result['msg'] !== 'success') {
            throw "error: " + result['msg']
        }

        return url + '/' + result['data']
    },

    "Catbox (有效期未知)": function(config) {
        let form = {
            'file': {
                'file': {// fileToUpload 为此网站上传文件的表单key
                    'fileToUpload': config
                },
                'fileName': "config.json",
                'contentType': "application/json"
            },
            'reqtype': 'fileupload',
        }
        let resp = ttsrv.httpPostMultipart('https://catbox.moe/user/api.php', form)
        if (resp.code() !== 200) {
            throw 'error: HTTP-' + resp.code()
        }

        return resp.body().string()
    }
}

function upload(url, config, extra) {
    let form = {
        "file": { // 嵌套的子Map, file为表单名
            'file': config, //上传内容
            'fileName': "config.json", // 文件名
            'contentType': "application/json" // 文件的mime
        },
    }

    return ttsrv.httpPostMultipart(url, form)
}