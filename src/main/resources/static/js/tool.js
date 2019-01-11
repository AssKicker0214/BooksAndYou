/**
 * css: tool.css
 */

Vue.component('tool', {
    props: {
        // more: Boolean,
        // text: String,
        highlighting: Boolean,
        waitingNext: Boolean,
        waitingPrevious: Boolean
    },
    template: // html
        `
        <aside class="tool">
            <button class="tool-control" @click="next">
                <i :class="['fas', waitingNext?'fa-sync rotate':'fa-chevron-right']"></i>
            </button>
            <button class="tool-control small" @click="previous">
                <i :class="['fas', waitingPrevious?'fa-sync rotate':'fa-chevron-left']"></i>
            </button>
            <input @keyup.enter="send" ref="input" class="tool-control small input">
            <button class="tool-control small" @click="switchHighlight"
                :class="[highlighting?'highlighting':'not-highlighting']">
                <i class="fas fa-highlighter"></i>
            </button>
        </aside>
        `,
    methods: {
        next: function(){
            this.$emit('vote-next');
        },
        previous: function(){
            this.$emit('vote-previous');
        },
        send: function(){
            this.$emit('send', this.$refs.input.value);
            this.$refs.input.value = "";
        },
        switchHighlight: function(){
            this.$emit('switch-highlighting')
        }
    }
})