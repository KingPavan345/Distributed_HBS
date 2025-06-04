# Admin CLI for Distributed Vacation HBS

This is the command-line interface (CLI) for admin management of the Distributed Vacation Home Booking System (HBS).

## Features
- **User Management:** List, view, update, and delete users
- **Listing Management:** Paginated browsing, view listing details, view listing reviews
- **Review Management:** List, view, update, and delete reviews (admins cannot add reviews)
- **JWT-based authentication**

## Setup
1. **Install dependencies:**
   ```sh
   pip install -r requirements.txt
   ```
2. **Run the CLI:**
   ```sh
   python menu_cli.py
   ```

## Usage
- On start, log in with your admin credentials.
- Use the menu to navigate between Users, Listings, and Reviews.
- Listings support interactive pagination.
- All review actions require authentication.
- Admins cannot add new reviews, but can update or delete existing ones.

## Environment
- Python 3.8+
- Requires access to running backend services (auth and listings microservices)

## Notes
- Make sure your backend services are running and accessible at the URLs configured in `menu_cli.py`.
- The CLI uses JWT tokens for authentication and will prompt for login if needed.

---
For any issues, please contact the project maintainer. 