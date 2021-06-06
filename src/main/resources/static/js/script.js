console.log("This is for showing and closing sidebar");

const toggleSidebar = () => {

    if($(".sidebar").is(":visible")) {
        // true 
        // close this
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else {
        // false 
        // show side bar
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
};

const search=() => {
    // console.log("searching....");
    // jquery 
    let query = $("#search-input").val();

    if(query == '') {
        $(".search-result").hide();
    }else {
        console.log(query);
        let url = `http://localhost:8080/search/${query}`;

        fetch(url).then((response) => {
                return response.json();
        })
        .then((data) => {
            console.log(data);

            let text = ` <div class='list-group'> `
                data.forEach(contact => {
                    text += `<a href= '/user/${conatct.cId}' class='list-group-item list-group-action'> ${contact.name} </a>`
                });
            text += `</div>`;

            $(".search-result").html(text);
            $(".search-result").show();
        });
    }
}