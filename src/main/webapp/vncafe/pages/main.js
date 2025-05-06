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

  const packageItem = document.querySelector("#packageItem");

  if (packageItem) {
    fetch("/vncafe/api/eventpackage")
      .then((response) => response.json())
      .then((data) => {
        renderEventPackages(data.packages);
      })
      .catch((error) => console.error("Error:", error));
  }

  function renderEventPackages(packages) {
    const container = document.getElementById("packageItem");
    container.innerHTML = "";

    packages.forEach((pack) => {
      const div = document.createElement("div");
      div.className = "col-md-4 mb-4";

      const descriptionItems = pack.description.split(",");
      const descriptionListItems = descriptionItems
        .map((item) => {
          return `<li data-aos="fade-left">${item.trim()}</li>`;
        })
        .join("");

      div.innerHTML = `
      <div class="card feature-card w-100 h-100">
        <div class="card-body">
          <h5 class="card-title" data-aos="fade-left">${pack.name}</h5>
          <h6 class="card-subtitle mb-2 text-muted" data-aos="fade-right">₹${pack.price}</h6>
          <ul class="list-unstyled">
            ${descriptionListItems}
          </ul>
          <a href="#booking" class="btn btn-outline-primary" data-aos="fade-up-left">Select Package</a>
        </div>
      </div>
    `;
      container.appendChild(div);
    });
  }
  // event booking form posting
  const eventForm = document.getElementById("bookingForm");
  if (eventForm) {
    eventForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      if (!this.checkValidity()) {
        this.classList.add("was-validated");
        return;
      }
      const userId = localStorage.getItem("user_id");
      const token = localStorage.getItem("token");

      if (!userId || !token) {
        alert("Please log in first.");
        return;
      }

      const bookingData = {
        user_id: parseInt(userId),
        event_booking: {
          package_id: getPackageId(document.getElementById("package").value),
          event_date: document.getElementById("date").value,
          special_requirement: document.getElementById("message").value,
        },
      };


      try {
        const response = await fetch("/vncafe/api/eventbooking", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            Authorization: `Bearer ${token}`
          },
          body: `input_data=${JSON.stringify(bookingData)}`,
        });

        const data = await response.json();
        console.log(data);

        if (data.response_status?.status === "success") {
          alert("Booking successful!");
          document.getElementById("bookingForm").reset();
        } else {
          alert(
            `Error: ${data.response_status?.message || "Something went wrong."}`
          );
        }
      } catch (err) {
        console.error("Error submitting booking:", err);
        alert("An error occurred while submitting the booking.");
      }
    });

    // Helper: Convert package string to backend ID
    function getPackageId(packageName) {
      switch (packageName.toLowerCase()) {
        case "basic":
          return 1;
        case "premium":
          return 2;
        case "deluxe":
          return 3;
        default:
          return 0;
      }
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
        if (errorMsg)
          errorMsg.textContent = "Please fill in all required fields.";
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
          if (errorMsg)
            errorMsg.textContent =
              data.response_status.message || "Invalid login.";
          // alert("Login failed: " + data.response_status.message);
          // console.log("Login failed response:", data);
          return;
        }

        localStorage.setItem("token", data.user.auth_token);
        localStorage.setItem("user", JSON.stringify(data.user));
        // alert("Signin successful!");
        window.location.href = "/vncafe/vncafe/pages/index.html";
        // console.log("Login success, user:", data.user);
      } catch (error) {
        if (errorMsg) errorMsg.textContent = error.message;
        console.error("Error during login:", error);
      }
    });
  }

  // === Signup ===
  const signupForm = document.getElementById("signup");
  const pwdErrorMsg = document.getElementById("pwdErrorMsg");
  const mailErrorMsg = document.getElementById("mailErrorMsg");
  if (signupForm) {
    signupForm.addEventListener("submit", async function (event) {
      event.preventDefault();
      errorMsg.textContent = "";
      pwdErrorMsg.textContent = "";
      mailErrorMsg.textContent = "";

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

      const payload = { "username":username, "email":email, "password":password };

      const formData = new URLSearchParams();
      formData.append("input_data", JSON.stringify(payload));

      try {
        const response = await fetch("/vncafe/api/signup", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData.toString(),
        });

        const data = await response.json();

        mailErrorMsg.textContent = data.response_status.message;
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
          // console.log("Menu Items:", data.menu);
          localStorage.setItem("menu", JSON.stringify(data.menu));

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

    document.getElementById("hotCoffeeDiv").innerHTML = "";
    document.getElementById("bakeryDiv").innerHTML = "";
    document.getElementById("coldCoffeeDiv").innerHTML = "";
    document.getElementById("ownProductDiv").innerHTML = "";

    container.innerHTML = "";
    menuItems.forEach((item) => {
      const card = createCoffeeCard(item);
      if (item.category.toLowerCase().includes(menuType)) {
        // console.log("Category----> " + item.category);
        // console.log("MENU TYPE---> " + menuType);
        container.appendChild(card);
      }
    });
  }

  function createCoffeeCard(item) {
    const col = document.createElement("div");
    col.className = "col-md-4 allCard";

    const cardHTML = `
        <div class="card menu-card" >
            <img src=${item.imgpath} class="menu-img card-img-top" data-aos="fade-zoom-in" alt="${item.name}">
            <div class="card-body">
                <h5 class="card-title" data-aos="zoom-out-right">${item.name}</h5>
                <p class="card-text text-muted mb-0" data-aos="fade-zoom-in">${item.description}</p>
                <p class="price" data-aos="fade-zoom-in">&#8377; ${item.price}</p>
                <span class="menuspan">
                    <button class="btn orderBtn cartBtn fw-bold" data-product-id='${item.id}' data-aos="fade-zoom-in">Add to Cart</button>
                    <button class="btn orderBtn buyNowBtn fw-bold" data-product-id='${item.id}' data-aos="fade-zoom-in">Buy Now</button>
                </span>
            </div>
        </div>
    `;

    col.innerHTML = cardHTML;
    return col;
  }
  document.addEventListener("click", function (e) {
    if (e.target.classList.contains("buyNowBtn")) {
      e.preventDefault();

      const token = localStorage.getItem("token");

      if (!token) {
        window.location.href = "/vncafe/vncafe/pages/profile_accounts.html";
      } else {
        window.location.href = "/vncafe/vncafe/pages/payment_page.html";
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
      let cartCount ; // You can update this value dynamically later

      navProfileBtn.innerHTML = `
      <div class="position-relative" style="display: inline-block;">
        <div class="position-relative d-inline-block me-3" style="cursor: pointer;">
        <img src="/vncafe/vncafe/assets/img/add-to-cart-icon-free-vector.jpg" role="button" id="cartIcon" width="40" height="40" class="" alt="Profile">
          <span id="cartCountBadge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            ${cartCount}
          </span>
        </div>
        <img src="/vncafe/vncafe/assets/person-circle.svg" role="button" id="profileToggle" width="40" height="40" class="rounded-circle" alt="Profile">
        <div id="profileCard" class="d-none position-absolute end-0 mt-2 p-3 bg-white shadow" style="width: 250px; z-index: 1000; border-radius: 8px;">
          <img src='/vncafe/vncafe/assets/img/demoprofileimage.jpeg' width="100" height="100" class="rounded-circle pb-2 user_profile_img" alt="User_profile_img"/>
          <p class="fw-bold mb-1">
            User Name: ${user.username}
          </p>
          <p class="text-muted small mb-2">Email: ${user.email}</p>
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
          localStorage.removeItem("token");
          localStorage.removeItem("menu_ids");
          window.location.href = "/vncafe/vncafe/pages/index.html";
        });
      }
    }
  } else {
    if (navProfileBtn) navProfileBtn.classList.add("d-none");
    if (signDivBtn) signDivBtn.classList.remove("d-none");
  }

  function addItem(productId) {
    let cart = JSON.parse(localStorage.getItem("menu_ids")) || [];
  
    const existingItem = cart.find(item => item.id === parseInt(productId));
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      cart.push({ id: parseInt(productId), quantity: 1 });
    }
  
    localStorage.setItem("menu_ids", JSON.stringify(cart));
    updateCartCount();
  }
  
  function getItems() {
    return JSON.parse(localStorage.getItem("menu_ids")) || [];
  }
  
  function updateCartCount() {
    let cart = [];
  
    try {
      const rawCart = localStorage.getItem("menu_ids");
      if (rawCart) {
        cart = JSON.parse(rawCart);
      }
    } catch (e) {
      console.error("Cart parse error, resetting cart.");
      localStorage.setItem("menu_ids", JSON.stringify([]));
      cart = [];
    }

    const totalCount = cart.reduce((sum, item) => sum + (item.quantity || 0), 0);
  
    const badge = document.getElementById("cartCountBadge");
  
    if (badge) {

      badge.textContent = totalCount || 0;
    }
  
    console.log("Updated cart count:", totalCount);
  }  
  
  document.addEventListener("click", (e) => {
    if (e.target.classList.contains("cartBtn")) {
      e.preventDefault();
  
      const token = localStorage.getItem("token");
  
      if (!token) {
        window.location.href = "/vncafe/vncafe/pages/profile_accounts.html";
      } else {
        const product_id = e.target.getAttribute("data-product-id");
        addItem(product_id);
  
        const payload = { ids: getItems() };
        const queryParams = new URLSearchParams();
        queryParams.append("input_data", JSON.stringify(payload));
  
        fetch(`/vncafe/api/menu?${queryParams.toString()}`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        })
          .then((response) => {
            console.log("Status Code:", response.status);
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            console.log("Updated cart data:", data);
          })
          .catch((error) => {
            console.error("Fetch error:", error);
          });
      }
    }
  });
  updateCartCount();
  
  const cartIcon = document.getElementById('cartIcon');
  if (cartIcon){

    cartIcon.addEventListener('click', ()=>{
      window.location.href = "/vncafe/vncafe/pages/checkout.html"
    })



  }


});

