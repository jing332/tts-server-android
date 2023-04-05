let SpeechRuleJS = {
    name: "旁白/对话",
    id: "ttsrv.multi_voice",
    author: "TTS Server",
    version: 1,
    tags: {narration: "旁白", dialogue: "对话"},

    handleText(text) {
        const list = [];
        let tmpStr = "";
        let endTag = "narration";

        text.split("").forEach((char, index) => {
            tmpStr += char;
            if (char === '“') {
                endTag = "dialogue";
                pushText(tmpStr, "narration")
                tmpStr = "";
            } else if (char === '”') {
                endTag = "narration";
                tmpStr = tmpStr.slice(0, -1);
                pushText(tmpStr, "dialogue")
                tmpStr = "";
            } else if (index === text.length - 1) {
                pushText(tmpStr, endTag)
            }
        });

        function pushText(str, tag){
            if (/^(\s|\p{C}|\p{P}|\p{Z}|\p{S})+$/.test(str) == false)
                list.push({text: str, tag: tag})
        }

        return list;
    },
};
