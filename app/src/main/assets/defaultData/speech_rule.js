let SpeechRuleJS = {
    name: "旁白/对话",
    id: "ttsrv.multi_voice",
    author: "TTS Server",
    version: 4,
    tags: {narration: "旁白", dialogue: "对话"},

    handleText(text) {
        const list = [];
        let tmpStr = "";
        let endTag = "narration";

        text.split("").forEach((char, index) => {
            tmpStr += char;

            if (char === '“') {
                endTag = "dialogue";
                list.push({text: tmpStr, tag: "narration"});
                tmpStr = "";
            } else if (char === '”') {
                endTag = "narration";
                tmpStr = tmpStr.slice(0, -1)
                list.push({text: tmpStr, tag: "dialogue"});
                tmpStr = "";
            } else if (index === text.length - 1) {
                list.push({text: tmpStr, tag: endTag});
            }
        });

        return list;
    },

    splitText(text) {
        let separatorStr = "。？?！!;；"

        let list = []
        let tmpStr = ""
        text.split("").forEach((char, index) => {
            tmpStr += char

            if (separatorStr.includes(char)) {
                list.push(tmpStr)
                tmpStr = ""
            } else if (index === text.length - 1) {
                list.push(tmpStr);
            }
        })

        return list.filter(item =>  item.replace(/[“”]/g, '').trim().length > 0);
    }

};
