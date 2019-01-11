const token = document.getElementById("token").textContent;
const debug = false;

let ws = {
    client: null,
    cmdPtn: /#(\S+)(?: (\S+)*)?/,
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
    // send: function (msg) {
    //     this.client.send('/desk/turn-page', {}, msg)
    // },
    send: function (msg) {
        if (msg === undefined || msg === null || msg === "") return;
        console.log("[ws send] " + msg);
        let groups = msg.match(this.cmdPtn);
        if (!groups) {
            this.client.send("/desk/chat", {}, JSON.stringify({
                token: token,
                name: 'chat',
                text: msg
            }))
        } else {
            let cmd = groups[1];
            let args = groups[2] === undefined?[]:groups[2].split(",");
            switch (cmd){
                case 'skip':    this.requestSkipPage(...args);break;
            }
        }
    },

    requestTurnPage: function(direction){
        console.info(`[ ws request turn page] -> ${direction}`)
        this.client.send('/desk/turn-page', {}, JSON.stringify({
            token: token,
            direction: direction,
            pages: 1
        }))
    },

    requestSkipPage: function(direction, pages){
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
        icon: "fa-comment-alt",
        header: "qhb",
        content: "asdfjlkasdjf发生了的咖啡机阿斯利康地方啊圣诞快乐房价爱丽丝的开发啊螺丝扣搭街坊按时灯笼裤发",
        show: false,
    },
    methods: {
        handleResponse: function (msg) {
            let res = JSON.parse(msg);
            let seg = res.token.split('-'); // [deskID, readerID]
            if (seg[0] !== token.split("-")[0]) return;

            switch (res.name) {
                case 'chat':
                    this.show = true;
                    console.log("[chat] -> " + res);
                    this.icon = "fa-comment-alt";
                    this.header = seg[1];
                    this.content = res.text;
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
                    if(res.ok){
                        main.current();
                    }
                    break;
                case 'skip-page':
                    console.log(`[skip page] -> ${res.ok}`);
                    if(res.ok){
                        main.current();
                    }
                    break;
            }
            setTimeout(()=>{
                this.show = false;
            }, 3000)
        },
    }



})

let main = new Vue({
    el: "#main",
    data: {
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
        send: function (s) {
            ws.send(s);
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
