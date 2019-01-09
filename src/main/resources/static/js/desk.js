const token = document.getElementById("token").textContent;

let ws = {
    client: null,
    connect: function(){
        let socket = new SockJS('/gs-guide-websocket');
        this.client = Stomp.over(socket);
        this.client.connect({}, frame => {
            console.info('[ws connected]' + frame);
            this.client.subscribe('/desk/greetings', (greetings)=>{
                console.log(greetings);
            })
        })
    },
    disconnect: function(){
        if(this.client !== null){
            this.client.disconnect();
            console.info('[ws disconnected]');
        }
    },
    send: function(){
        this.client.send('/desk/hello', {}, 'client')
    }
};

let main = new Vue({
    el: "#main",
    data: {
        chars: 400,
        selecting: false,
        html: ""
    },
    methods: {
        get: function (index) {
            const url = `/get-page?index=${index}`;
            fetch(url, {
                method: 'get'
            }).then(res => {
                if (res.ok) {
                    console.dir(res);
                    res.json().then(j => {
                        console.dir(j);
                    })
                }
            })
        },

        next: function(){
            this.turn('next');
        },

        last: function(){
            this.turn('last');
        },

        current: function(){
            this.turn('current');
        },

        turn: function (direction) {
            const url = `/turn-page?token=${token}&direction=${direction}`;
            fetch(url, {
                method: 'get'
            }).then(res => {
                if (res.ok) {
                    res.json().then(elems => {
                        es = elems;
                        this.html = elems.map(elem => {
                            let list = elem.split("");
                            let change = false;
                            list.forEach((char, i, a) => {
                                if(char === "<")    change = false;
                                
                                if(change){
                                    a[i] = `<span class="char">${char}</span>`;
                                }
                                if(char === ">")    change = true;
                            });
                            return list.join('');
                        }).join('\n');

                        console.log(this.html);
                    })
                }
            })
        },

        dump: function(){
            const url = `/dump?token=${token}`;
            fetch(url, {
                method: 'get'
            }).then(res => {
                if(res.ok){
                    res.text().then(txt => {
                        console.info(`[dump] -> ${txt}`)
                    })
                }
            })
        }
    },

    created: function(){
        this.current();
    }

});
