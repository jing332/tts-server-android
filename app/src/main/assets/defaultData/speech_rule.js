let SpeechRuleJS = {
    name: "MultiVoice",
    id: "ttsrv.multi_voice",
    author: "samsonsin",
    version: 1,
    tags: {
        narration: "Narration",
        dialogue: "Dialogue",
        squareBrackets: "Square Brackets",
        curlyBrackets: "Curly Brackets",
    },

    handleText(text) {
        //Define regex patterns for different text types. The order matters, as the first match takes precedence.
        let regexExpressions = [
            { regex: /.+/g, tag: "narration" },
            { regex: /["“][^"“”]*["”]?/g, tag: "dialogue" },
            { regex: /\\[([^\\]]*?\\s+[^\\]]*?\\s+[^\\]]*?)\\]/g, tag: "squareBrackets" }, 
            { regex: /{[^}]*?}/g, tag: "curlyBrackets" },

            //Final rule to match and ignore specific characters by assigning them a null tag.
            { regex: /[•*{}"“”]/g, tag: null}
        ];

        //Initialize an array to hold the regex matches.
        let regexMatches = new Array(text.length + 1).fill(null);

        //Iterate through each regex pattern and assign the corresponding tag to the matched char in the regexMatches array
        regexExpressions.forEach((rule) => {
            while ((queryResult = rule.regex.exec(text)) !== null) {
                //If the match is empty, increment the last index of the regex and continue to the next iteration, skipping the char
                if (queryResult[0].length === 0) {
                    rule.regex.lastIndex++;
                    continue;
                }
                regexMatches.fill(
                    rule.tag,
                    queryResult.index,
                    rule.regex.lastIndex
                );
            }
        });

        //two moving indices to track the current position in the text and the end of last segment. When discovering a new segment,
        //push current segment to output array and update the previous tag and index. regexMatches final index is null to ensure the last segment is captured.
        let previousTag = regexMatches[0];
        let previousIndex = 0;
        let textSegments = [];
        for (let i = 1; i < regexMatches.length; i++) {
            var isInMiddleOfTextSegment = previousTag === regexMatches[i];
            var isCurrentSelectionEmptyOrContainsNull =
                text.slice(previousIndex, i).trim().length === 0 ||
                previousTag === null;

            if (isInMiddleOfTextSegment) continue;
            if (isCurrentSelectionEmptyOrContainsNull) {
                previousTag = regexMatches[i];
                previousIndex = i;
                continue;
            }

            textSegments.push({
                text: text.slice(previousIndex, i).trim(),
                tag: previousTag,
            });
            previousTag = regexMatches[i];
            previousIndex = i;
        }
        return textSegments;
    },

    splitText(text) {
        //Regex matches immediately after specific characters, splitting the text into segments.
        //Then asserts that each segment, after removing some characters, is non-empty.
        //This implementation leverages regex operations rather than custom made logic, and is equivalent to the original in my testing
        return (text
            .split(/[。？?！!;；]\\K/g)
            .filter((item) => item.replace(/[“”]/g, "").trim().length > 0));
    },
};
