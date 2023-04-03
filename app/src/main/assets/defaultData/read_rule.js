let ReadRuleJS = {
    "name": "示例规则",
    "id": "rule123",
    "author": "TTS Server",
    "version": 1,
    "tags": {"narration": "旁白", "dialogue": "对话"},

    "handleText": function(text){
        let arr = new Array()
        text.split(",").forEach(function(value){
            arr.push({"text": value, "tag": "TAG"})
        })

        return arr
    },
}