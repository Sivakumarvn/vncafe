document.addEventListener("DOMContentLoaded", function () {
  AOS.init({ duration: 1000, once: true });

  const errorMsg = document.getElementById("errorMsg");

  // === Carousel Logic ===
  const track = document.getElementById("carouselTrack");
  if (track) {
    let isMoving = false;

    const getCardWidth = () => {
      const card = track.querySelector(".carousel-card");
      const style = window.getComputedStyle(card);
      const marginRight = parseInt(style.marginRight);
      return card.offsetWidth + marginRight;
    };

    const moveNext = () => {
      if (isMoving) return;
      isMoving = true;

      const cardWidth = getCardWidth();
      track.style.transition = "transform 0.4s ease-in-out";
      track.style.transform = `translateX(-${cardWidth}px)`;

      track.addEventListener(
        "transitionend",
        () => {
          track.style.transition = "none";
          track.appendChild(track.children[0]);
          track.style.transform = "translateX(0)";
          isMoving = false;
        },
        { once: true }
      );
    };

    const movePrev = () => {
      if (isMoving) return;
      isMoving = true;

      const cardWidth = getCardWidth();
      track.style.transition = "none";
      track.insertBefore(track.lastElementChild, track.firstElementChild);
      track.style.transform = `translateX(-${cardWidth}px)`;

      requestAnimationFrame(() => {
        track.style.transition = "transform 1s ease-in-out";
        track.style.transform = "translateX(0)";
      });

      track.addEventListener(
        "transitionend",
        () => {
          isMoving = false;
        },
        { once: true }
      );
    };

    document.getElementById("nextBtn")?.addEventListener("click", moveNext);
    document.getElementById("prevBtn")?.addEventListener("click", movePrev);

    let autoScroll = setInterval(moveNext, 4000);
    track.addEventListener("mouseenter", () => clearInterval(autoScroll));
    track.addEventListener("mouseleave", () => {
      autoScroll = setInterval(moveNext, 4000);
    });
  }
  // Carousal nav redirect to menu
  document.addEventListener("DOMContentLoaded", function () {
    const hash = document.window.hash;
    if (hash === "#ownProduct") {
      const tabTrigger = document.querySelector(
        'button[data-bs-target="' + hash + '"]'
      );
      if (tabTrigger) {
        let tab = new bootstrap.Tab(tabTrigger);
        tab.show();
      }
    }
  });
  // event form validation
  const bookingForm = document.getElementById("bookingForm");
  if (bookingForm) {
    bookingForm.addEventListener("submit", function (event) {
      event.preventDefault();
      if (!this.checkValidity()) {
        event.stopPropagation();
      } else {
        alert(
          "Thank you for your booking! We will contact you soon to confirm the details."
        );
        this.reset();
      }
      this.classList.add("was-validated");
    });

    const dateInput = document.getElementById("date");
    if (dateInput) {
      const today = new Date().toISOString().split("T")[0];
      dateInput.setAttribute("min", today);
    }
  }

  // === Login ===
  const loginForm = document.getElementById("login");
  if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
      event.preventDefault();

      const email = document.getElementById("loginUsername").value;
      const password = document.getElementById("loginPassword").value;

      if (!email || !password) {
        if (errorMsg) errorMsg.textContent = "Please fill in all required fields.";
        return;
      }

      const payload = { email, password };
      const formData = new URLSearchParams();
      formData.append("input_data", JSON.stringify(payload));

      try {
        const response = await fetch("/vncafe/api/signin", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData.toString(),
        });


        if (!response.ok) {
          throw new Error("Network response was not ok");
        }

        const data = await response.json();

        if (data.response_status.status_code !== 2000) {
          if (errorMsg) errorMsg.textContent = data.response_status.message || "Invalid login.";
          // alert("Login failed: " + data.response_status.message);
          // console.log("Login failed response:", data);
          return;
        }

        localStorage.setItem("token", data.user.auth_token);
        localStorage.setItem("user", JSON.stringify(data.user));
        alert("Signin successful!");
        window.location.href= "/vncafe/vncafe/pages/index.html";
        // console.log("Login success, user:", data.user);

       
      } catch (error) {
        if (errorMsg) errorMsg.textContent = error.message;
        console.error("Error during login:", error);
      }
    });
  }

  // === Signup ===
  const signupForm = document.getElementById("signup");
  const pwdErrorMsg= document.getElementById('pwdErrorMsg');
  const mailErrorMsg = document.getElementById('mailErrorMsg');
  if (signupForm) {
    signupForm.addEventListener("submit", async function (event) {
      event.preventDefault();
      errorMsg.textContent = "";
      pwdErrorMsg.textContent='';
      mailErrorMsg.textContent='';

      const email = document.getElementById("signupEmail").value;
      const username = document.getElementById("signupUsername").value;
      const password = document.getElementById("signupPassword").value;
      const confirmPassword = document.getElementById(
        "signupConfirmPassword"
      ).value;

      if (!email || !username || !password || !confirmPassword) {
        errorMsg.textContent = "Please fill in all required fields.";
        return;
      }

      if (password !== confirmPassword) {
        pwdErrorMsg.textContent = "Passwords doesn't match.";
        return;
      }

      const payload = { username, email, password };
      const formData = new URLSearchParams();
      formData.append("input_data", JSON.stringify(payload));

      try {
        const response = await fetch("/vncafe/api/signup", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData.toString(),
        });

        const data = await response.json();
        
        mailErrorMsg.textContent= data.response_status.message;
        console.log(data);
        if (!response.ok)
          throw new Error(data.message || "Something went wrong");
        alert("Signup successful!");
      } catch (error) {
        errorMsg.textContent = error.message;
      }
    });
  }

  // === Menu Item Fetch ===
  if (window.location.pathname.includes("menu")) {
    if (window.location.pathname.includes("menu")) {
      fetch("/vncafe/api/menu")
        .then((response) => response.json())
        .then((data) => {
          console.log("Menu Items:", data.menu);
          renderCoffeeMenu(data.menu, "hotCoffeeDiv", "hot");
        })
        .catch((error) => console.error("Error:", error));
    }
  }
  const coffeeTab = document.getElementById("coffee-tab");
  if (coffeeTab) {
    coffeeTab.addEventListener("click", function () {
      fetch("/vncafe/api/menu")
        .then((response) => response.json())
        .then((data) => {
          console.log("Menu Items:", data.menu);
          renderCoffeeMenu(data.menu, "hotCoffeeDiv", "hot");
        })
        .catch((error) => console.error("Error:", error));
    });
  }
  const coldTab = document.getElementById("cold-tab");
  if (coldTab) {
    coldTab.addEventListener("click", function () {
      fetch("/vncafe/api/menu")
        .then((response) => response.json())
        .then((data) => {
          console.log("Menu Items:", data.menu);
          renderCoffeeMenu(data.menu, "coldCoffeeDiv", "cold");
        })
        .catch((error) => console.error("Error:", error));
    });
  }
  const bakeryTab = document.getElementById("bakery-tab");
  if (bakeryTab) {
    bakeryTab.addEventListener("click", function () {
      fetch("/vncafe/api/menu")
        .then((response) => response.json())
        .then((data) => {
          console.log("Menu Items:", data.menu);
          renderCoffeeMenu(data.menu, "bakeryDiv", "bakery");
        })
        .catch((error) => console.error("Error:", error));
    });
  }
  const productTab = document.getElementById("Product-tab");
  if (productTab) {
    productTab.addEventListener("click", function () {
      fetch("/vncafe/api/menu")
        .then((response) => response.json())
        .then((data) => {
          console.log("Menu Items:", data.menu);
          renderCoffeeMenu(data.menu, "ownProductDiv", "own");
        })
        .catch((error) => console.error("Error:", error));
    });
  }
  function renderCoffeeMenu(menuItems, containerId, menuType) {
    const container = document.getElementById(containerId);
    // Clear old content
    document.getElementById("hotCoffeeDiv").innerHTML = "";
    document.getElementById("bakeryDiv").innerHTML = "";
    document.getElementById("coldCoffeeDiv").innerHTML = "";
    document.getElementById("ownProductDiv").innerHTML = "";

    container.innerHTML = "";
    menuItems.forEach((item) => {
      const card = createCoffeeCard(item);
      if (item.category.toLowerCase().includes(menuType)) {
        console.log("Category----> " + item.category);
        console.log("MENU TYPE---> " + menuType);
        container.appendChild(card);
      }
    });
  }

  function createCoffeeCard(item) {
    const col = document.createElement("div");
    col.className = "col-md-4 allCard";

    const cardHTML = `
        <div class="card menu-card ">
            <img src=${item.imgpath} class="menu-img card-img-top" alt="${
      item.name
    }">
            <div class="card-body">
                <h5 class="card-title">${item.name}</h5>
                <p class="card-text text-muted">${item.description}</p>
                <span class="menuspan">
                    <p class="price">&#8377; ${item.price}</p>
                    <button class="btn orderBtn" data-product='${JSON.stringify(
                      item
                    )}'>Buy Now</button>
                </span>
            </div>
        </div>
    `;

    col.innerHTML = cardHTML;
    return col;
  }
  document.addEventListener("click", function (e) {
    if (e.target.classList.contains("orderBtn")) {
      e.preventDefault();

      const token = localStorage.getItem("token"); // Check login

      if (!token) {
        // Redirect to login page if not logged in
        window.location.href = "/vncafe/vncafe/pages/login.html";
      } else {
        // If logged in, get product and redirect to checkout
        const product = JSON.parse(e.target.getAttribute("data-product"));
        localStorage.setItem("selectedProduct", JSON.stringify(product));
        window.location.href = "/vncafe/vncafe/pages/checkout.html";
      }
    }
  });

  //  profile icon show user details

  const user = JSON.parse(localStorage.getItem("user"));
  const token = localStorage.getItem("token");
  
  const navProfileBtn = document.getElementById("navProfileBtn");
  const signDivBtn = document.getElementById("signDivBtn");
  
  if (user && token) {
    if (signDivBtn) {
      signDivBtn.classList.add("d-none");
    }
    if (navProfileBtn) {
      navProfileBtn.classList.remove("d-none");

      navProfileBtn.innerHTML = `
        <div class="position-relative" style="display: inline-block;">
          <img src="/vncafe/vncafe/assets/person-circle.svg" role="button" id="profileToggle" width="40" height="40" class="rounded-circle" alt="Profile">
          <div id="profileCard" class="d-none position-absolute end-0 mt-2 p-3 bg-white shadow" style="width: 250px; z-index: 1000; border-radius: 8px;">
            <img src='/vncafe/vncafe/assets/img/demoprofileimage.jpeg' width="100" height="100" class="rounded-circle pb-2 user_profile_img" alt="User_profile_img"/>
            <p class="fw-bold mb-1">User Name: ${user.username}</p>
            <p class="text-muted small mb-2"> Email: ${user.email}</p>
            <button id="logoutBtn" class="btn btn-sm btn-danger all-btn w-100">Logout</button>
          </div>
        </div>
      `;
  
      const profileToggle = document.getElementById("profileToggle");
      const profileCard = document.getElementById("profileCard");
      const logoutBtn = document.getElementById("logoutBtn");

      if (profileToggle && profileCard) {
        profileToggle.addEventListener("click", () => {
          profileCard.classList.toggle("d-none");
        });
      }
      document.addEventListener("click", (e) => {
        if (!navProfileBtn.contains(e.target)) {
          if (profileCard) {
            profileCard.classList.add("d-none");
          }
        }
      });
  
      if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
          localStorage.removeItem("user");
          localStorage.removeItem("token");
          window.location.href = "/vncafe/vncafe/pages/index.html";
        });
      }
    }
  
  } else {
    if (navProfileBtn) navProfileBtn.classList.add("d-none");
    if (signDivBtn) signDivBtn.classList.remove("d-none");
  }
  
});
