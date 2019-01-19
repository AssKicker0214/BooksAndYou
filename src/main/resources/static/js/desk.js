const token = document.getElementById("token").textContent;
const debug = false;

let ws = {
    client: null,
    connect: function () {
        let socket = new SockJS('/gs-guide-websocket');
        this.client = Stomp.over(socket);
        this.client.connect({}, frame => {
            console.info('[ws connected]' + frame);
            this.client.subscribe('/desk/controller', (res) => {
                console.log("[ws received] " + res.body);
                responsor.handleResponse(res.body);
            })
        });
        if (!debug) this.client.debug = () => { }
    },
    disconnect: function () {
        if (this.client !== null) {
            this.client.disconnect();
            console.info('[ws disconnected]');
        }
    },

    chat: function (msg) {
        this.client.send("/desk/chat", {}, JSON.stringify({
            token: token,
            name: 'chat',
            text: msg
        }))
    },

    requestTurnPage: function (direction) {
        console.info(`[ ws request turn page] -> ${direction}`)
        this.client.send('/desk/turn-page', {}, JSON.stringify({
            token: token,
            direction: direction,
            pages: 1
        }))
    },

    requestSkipPage: function (direction, pages) {
        console.info(`[ws request skip page] -> ${direction} ${pages}`);
        this.client.send('/desk/skip-page', {}, JSON.stringify({
            token: token,
            direction: direction,
            skip: pages
        }))
    },

    /**
     * 
     * @param {int} start 
     * @param {int} end 
     */
    requestHighlight: function (start, end) {
        console.info(`[ws request highlight] -> ${start} - ${end}`);
        this.client.send('/desk/highlight', {},
            JSON.stringify({
                token: token,
                name: 'highlight',
                start: start,
                end: end
            })
        )
    }
};

let responsor = new Vue({
    el: "#responsor",
    data: {
        list: [
            /* {
                icon: "fa-comment-alt",
                header: "qhb",
                content: ""
            } */
        ],
        show: false,
        maxCache: 3,
        responsorTimer: 3000,
    },
    methods: {
        handleResponse: function (msg) {
            let res = JSON.parse(msg);
            let seg = res.token.split('-'); // [deskID, readerID]
            if (seg[0] !== token.split("-")[0]) return;

            switch (res.name) {
                case 'chat':
                    this.append("fa-comment-alt", seg[1], res.text);
                    this.resetShow(true);
                    break;
                case 'highlight':
                    let start = parseInt(res.start);
                    let end = parseInt(res.end);
                    let chars = document.querySelectorAll('.char');
                    chars.forEach(char => {
                        char.classList.remove("highlighting-char")
                        let id = parseInt(char.id);
                        if (id >= start && id <= end) {
                            char.classList.add("highlighting-char")
                        }
                    });
                    break;
                case 'turn-page':
                    console.log(`[turn page] -> ${res.ok}`);
                    if (res.ok) {
                        main.current();
                    }
                    break;
                case 'skip-page':
                    console.log(`[skip page] -> ${res.ok}`);
                    if (res.ok) {
                        main.current();
                    }
                    break;
            }

        },

        append: function(icon, header, content){
            if(this.list.length >= this.maxCache){
                this.list.shift();
            }
            this.list.push({
                icon: icon,
                header: header,
                content: content
            })
        },

        resetShow: function (show) {
            this.responsorTimer = 3000;
            this.show = show;
        }
    },

    created: function () {
        setInterval(() => {
            if (this.responsorTimer <= 0) {
                this.show = false;
            } else {
                this.responsorTimer -= 500;
            }
        }, 500)
    }

})

let main = new Vue({
    el: "#main",
    data: {
        nightMode: false,
        placeholder: [0, 0],

        chars: 400,
        selecting: false,
        html: "",

        // tool
        text: "",
        highlighting: false,
        highlightRange: [],

        waitingNext: false,
        waitingPrevious: false,

    },
    methods: {
        voteNext: function () {
            this.waitingNext = true;
            this.waitingPrevious = false;
            this.checkTurn('next')
            // this.turn('next');
        },

        votePrevious: function () {
            this.waitingNext = false;
            this.waitingPrevious = true;
            this.checkTurn('previous');
        },

        current: function () {
            this.turn('current');
            this.waitingNext = this.waitingPrevious = false;

        },

        checkTurn: function (direction) {
            ws.requestTurnPage(direction)
        },

        /**
         * 获取上一页/下一页，并将文字包装为<span class='char'></span>
         * @param {String} direction 
         */
        turn: function (direction) {
            const url = `/turn-page?token=${token}&direction=${direction}`;
            fetch(url, {
                method: 'get'
            }).then(res => {
                if (res.ok) {
                    res.json().then(elems => {
                        es = elems;
                        let idx = 0;
                        this.html = elems.map(elem => {
                            let list = elem.split("");
                            let change = false;
                            list.forEach((char, i, a) => {
                                if (char === "<") change = false;

                                if (change) {
                                    a[i] = /*html*/
                                        `<span  class="char" 
                                            id="${idx}">
                                            ${char}
                                    </span>`;
                                    idx++;
                                }
                                if (char === ">") change = true;
                            });
                            return list.join('');
                        }).join('\n');
                    });
                }
            })
        },

        contentHtml: function(){
            return /*html*/`<div style="float: left; width: ${this.placeholder[0]}px; height: ${this.placeholder[1]}px;"></div>\n` + this.html;
        },

        /**
         * 命令服务器保存当前desk的状态
         */
        dump: function () {
            const url = `/dump?token=${token}`;
            fetch(url, {
                method: 'get'
            }).then(res => {
                if (res.ok) {
                    res.text().then(txt => {
                        console.info(`[dump] -> ${txt}`)
                    })
                }
            })
        },

        /**
         * 发送字符串
         */
        send: function (msg) {
            if (msg === undefined || msg === null || msg === "") return;

            const cmdPtn = /^#(\S+)(?: (\S+)*)?/;
            let groups = msg.match(cmdPtn);
            let cmd = null;
            let args = null;
            if (!groups) {
                cmd = 'chat';
                args = [msg];
            } else {
                cmd = groups[1];
                args = groups[2] === undefined ? [] : groups[2].split(",");
            }
            console.info(`${cmd}(${args.join(", ")})`);
            switch (cmd) {
                case 'chat': ws.chat(...args); break
                case 'skip': ws.requestSkipPage(...args); break;
                case 'day': this.nightMode = false; break;
                case 'night': this.nightMode = true; break;
                case 'placeholder': this.placeholder = new Array(...args); break;
            }
        },

        selectHighlightingRange: function () {
            if (this.highlighting && window.event.target.matches(".char")) {
                if (this.highlightRange.length === 2) {
                    this.highlightRange = []
                }

                window.event.target.classList.add('highlighting-char')
                this.highlightRange.push(parseInt(window.event.target.id));
                this.highlightRange.sort();
                console.table(this.highlightRange);


                if (this.highlightRange.length === 2) {
                    ws.requestHighlight(...this.highlightRange);
                }
            }
        },

        switchHighlighting: function () {
            this.highlighting = !this.highlighting;
        },

        isHighlightChar: function (idx) {
            if (highlightRange.length === 2 && idx >= highlightRange[0] && idx <= highlightRange[1]) {
                return true;
            } else {
                return false;
            }

        }
    },

    created: function () {
        this.current();
    }

});
