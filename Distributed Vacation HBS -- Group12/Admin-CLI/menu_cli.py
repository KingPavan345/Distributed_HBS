"""
menu_cli.py

Admin CLI for Distributed Vacation HBS (Hotel Booking System)

This script provides a command-line interface for admin users to manage users, listings, and reviews in the distributed vacation home booking system. It interacts with backend microservices for authentication and data management.

Authors:
- Mohammad Shajadul Karim (Admin-CLI)
"""
import requests
import sys
import re
from getpass import getpass
from typing import Dict, Any, Optional, Union
from rich.console import Console
from rich.table import Table
from rich.prompt import Prompt, Confirm
from tabulate import tabulate
import jwt

# --- Configuration ---
AUTH_SERVICE_BASE_URL = "http://localhost:8000/api/auth/"
ADMIN_AUTH_BASE_URL = "http://localhost:8000/admin/auth/"
# Assuming listings service runs on port 8001
LISTINGS_SERVICE_BASE_URL = "http://localhost:8001/api/listings/"
ADMIN_LISTINGS_BASE_URL = "http://localhost:8001/admin/listings/" 
ADMIN_REVIEWS_BASE_URL = "http://localhost:8001/admin/reviews/" 
console = Console()

# --- Session Management ---
class UserSession:
    def __init__(self):
        self.access_token = None
        self.refresh_token = None
        self.is_admin = False

session = UserSession()

def get_auth_headers() -> Dict[str, str]:
    headers = {}
    if session.access_token:
        headers['Authorization'] = f'Bearer {session.access_token}'
    return headers

def login():
    console.rule("[bold blue]Admin Login")
    username = Prompt.ask("Username")
    password = getpass("Password: ")
    try:
        response = requests.post(
            f"{AUTH_SERVICE_BASE_URL}login/",
            json={"username": username, "password": password}
        )
        if response.status_code == 200:
            data = response.json()
            session.access_token = data.get('access')
            session.refresh_token = data.get('refresh')
            session.is_admin = True
            console.print("[green]Login successful![/green]")
            return True
        else:
            console.print(f"[red]Login failed: {response.text}[/red]")
            return False
    except Exception as e:
        console.print(f"[red]Login failed:[/red] {str(e)}")
        return False

def make_request(method: str, url: str, data: Optional[Dict[str, Any]] = None) -> Union[Dict[str, Any], list, None]:
    try:
        response = requests.request(
            method,
            url,
            json=data,
            headers=get_auth_headers()
        )
        response.raise_for_status()
        if response.text:
            return response.json()
        return []  # Return empty list instead of None for empty responses
    except requests.exceptions.RequestException as e:
        if hasattr(e, 'response') and e.response is not None:
            if e.response.status_code == 400:
                try:
                    error_json = e.response.json()
                    console.print("[red]There was a problem with your input:")
                    if isinstance(error_json, dict):
                        for field, messages in error_json.items():
                            if isinstance(messages, list):
                                for msg in messages:
                                    console.print(f"[yellow]{field}[/yellow]: {msg}")
                            else:
                                console.print(f"[yellow]{field}[/yellow]: {messages}")
                    elif isinstance(error_json, list):
                        for msg in error_json:
                            console.print(f"[yellow]Error[/yellow]: {msg}")
                    else:
                        console.print(f"[yellow]Error[/yellow]: {error_json}")
                except Exception:
                    console.print(f"[red]Error:[/red] Bad Request")
                return None
        console.print(f"[red]Error:[/red] {str(e)}")
        return None

def display_table(data, fields, title: str = ""):
    if not data:
        print("No data to display.")
        return

    # Map API fields to desired table columns
    field_mapping = {
        'id': '_id',
        'title': 'name',
        'created_at': 'last_scraped',
        'description': 'description',
        'price': 'price',
        'amenities': 'amenities',
        'reviews': 'reviews'
    }

    # Use the mapped field names for the table
    headers = [field_mapping.get(field, field) for field in fields]
    
    # Process the data to ensure IDs are displayed
    rows = []
    for item in data:
        row = []
        for field in fields:
            if field == '_id':
                # Use listing_id if present, else _id
                row.append(item.get('listing_id', item.get('_id', '')))
            else:
                row.append(item.get(field_mapping.get(field, field), ''))
        rows.append(row)

    # Print the table
    print(tabulate(rows, headers=headers, tablefmt='grid'))

def validate_email(email: str) -> bool:
    """Validate email format."""
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return bool(re.match(pattern, email))

def validate_password(password: str) -> tuple[bool, str]:
    """Validate password requirements."""
    if len(password) < 8:
        return False, "Password must be at least 8 characters long"
    if not re.search(r'\d', password):
        return False, "Password must contain at least one number"
    if not re.search(r'[A-Z]', password):
        return False, "Password must contain at least one uppercase letter"
    return True, ""

def user_menu():
    while True:
        console.rule("[bold blue]User Management")
        console.print("[1] List users\n[2] View user\n[3] Create user\n[4] Update user\n[5] Delete user\n[0] Back")
        choice = Prompt.ask("Choose an option", choices=["1","2","3","4","5","0"])
        if choice == "1":
            users = make_request('GET', f"{AUTH_SERVICE_BASE_URL}users/")
            if users is not None:
                if isinstance(users, list) and len(users) > 0:
                    display_table(users, ['username', 'email', 'is_active', 'is_staff'], title="Users")
                else:
                    console.print("[yellow]No users found[/yellow]")
        elif choice == "2":
            username = Prompt.ask("Username to view")
            user = make_request('GET', f"{AUTH_SERVICE_BASE_URL}users/{username}/")
            if user:
                console.print_json(data=user)
            else:
                console.print("[yellow]User not found[/yellow]")
        elif choice == "3":
            while True:
                username = Prompt.ask("Username")
                if not username:
                    console.print("[red]Username is required[/red]")
                    continue
                
                email = Prompt.ask("Email")
                if not validate_email(email):
                    console.print("[red]Invalid email format. Please try again.[/red]")
                    continue
                
                password = getpass("Password: ")
                is_valid, error_msg = validate_password(password)
                if not is_valid:
                    console.print(f"[red]{error_msg}. Please try again.[/red]")
                    continue
                
                data = {
                    'username': username,
                    'email': email,
                    'password': password,
                    'is_active': True,
                    'is_staff': False
                }
                result = make_request('POST', f"{AUTH_SERVICE_BASE_URL}register/", data)
                if result:
                    console.print("[green]User created successfully![/green]")
                    break
                else:
                    console.print("[red]Failed to create user. Please try again.[/red]")
                    continue
        elif choice == "4":
            username = Prompt.ask("Username to update")
            is_active = Confirm.ask("Set user active?", default=None)
            is_staff = Confirm.ask("Set user as staff?", default=None)
            password = Prompt.ask("New password (leave blank to skip)", default="")
            data = {}
            if is_active is not None:
                data['is_active'] = is_active
            if is_staff is not None:
                data['is_staff'] = is_staff
            if password:
                data['password'] = password
            if data:
                result = make_request('PATCH', f"{AUTH_SERVICE_BASE_URL}users/{username}/", data)
                if result:
                    console.print("[green]User updated successfully![/green]")
            else:
                console.print("[yellow]No updates provided[/yellow]")
        elif choice == "5":
            username = Prompt.ask("Username to delete")
            if Confirm.ask(f"Are you sure you want to delete user {username}?"):
                result = make_request('DELETE', f"{AUTH_SERVICE_BASE_URL}users/{username}/")
                if result is not None:
                    console.print("[green]User deleted successfully![/green]")
        elif choice == "0":
            break

def listing_menu():
    while True:
        console.rule("[bold blue]Listing Management")
        console.print("[1] List all listings\n[2] View listing details\n[3] View listing reviews\n[0] Back")
        console.print("[yellow]Note: Create, update, and delete operations are not implemented as they were not required in the project description.[/yellow]")
        choice = Prompt.ask("Choose an option", choices=["1","2","3","0"])
        if choice == "1":
            page = 1
            limit = 10
            while True:
                listings = make_request('GET', f"{LISTINGS_SERVICE_BASE_URL}?page={page}&limit={limit}")
                if listings and isinstance(listings, dict) and 'data' in listings:
                    display_table(listings['data'], [
                        '_id', 
                        'name', 
                        'price', 
                        'bedrooms',
                        'property_type',
                        'review_scores_rating'
                    ], title=f"Listings (Page {listings['pagination'].get('current_page', page)})")
                    pagination = listings.get('pagination', {})
                    nav_options = []
                    if pagination.get('has_prev'):
                        nav_options.append('B')
                    if pagination.get('has_next'):
                        nav_options.append('N')
                    nav_options.append('Q')
                    nav = Prompt.ask(f"[N]ext page, [B]ack page, [Q]uit", choices=[o for o in nav_options], default='Q').upper()
                    if nav == 'N' and pagination.get('has_next'):
                        page += 1
                    elif nav == 'B' and pagination.get('has_prev'):
                        page -= 1
                    else:
                        break
                else:
                    console.print("[yellow]No listings found or unexpected response format.[/yellow]")
                    break
        elif choice == "2":
            listing_id = Prompt.ask("Listing ID to view")
            listing = make_request('GET', f"http://localhost:8001/api/listing/{listing_id}/")
            if listing and isinstance(listing, dict) and 'data' in listing:
                console.print_json(data=listing['data'])
            else:
                console.print("[red]Listing not found or error occurred.[/red]")
        elif choice == "3":
            listing_id = Prompt.ask("Listing ID to view reviews")
            reviews = make_request('GET', f"http://localhost:8001/api/listing/{listing_id}/reviews/")
            if reviews and isinstance(reviews, dict) and 'reviews' in reviews:
                display_table(reviews['reviews'], [
                    '_id', 'reviewer_name', 'comments', 'date'
                ], title=f"Reviews for Listing {listing_id}")
            else:
                console.print("[yellow]No reviews found or error occurred.[/yellow]")
        elif choice == "0":
            break

def review_menu():
    while True:
        console.rule("[bold blue]Review Management")
        console.print("[1] List reviews for a listing\n[2] View review details\n[4] Update review\n[5] Delete review\n[0] Back")
        choice = Prompt.ask("Choose an option", choices=["1","2","4","5","0"])
        if choice == "1":
            home_id = Prompt.ask("Home ID")
            if not home_id:
                console.print("[red]Home ID cannot be empty.[/red]")
                continue
            # Use the correct singular endpoint for listing reviews
            reviews = make_request('GET', f"http://localhost:8001/api/listing/{home_id}/reviews/")
            if reviews and isinstance(reviews, dict) and 'reviews' in reviews:
                display_table(reviews['reviews'], [
                    '_id', 'reviewer_name', 'rating', 'comments', 'date'
                ], title=f"Reviews for Listing {home_id}")
            else:
                console.print("[yellow]No reviews found for this listing or error occurred.[/yellow]")
        elif choice == "2":
            home_id = Prompt.ask("Home ID")
            if not home_id:
                console.print("[red]Home ID cannot be empty.[/red]")
                continue
            review_id = Prompt.ask("Review ID")
            if not review_id:
                console.print("[red]Review ID cannot be empty.[/red]")
                continue
            listing = make_request('GET', f"{LISTINGS_SERVICE_BASE_URL}{home_id}/")
            reviews = listing.get('reviews', []) if isinstance(listing, dict) else []
            review = next((r for r in reviews if isinstance(r, dict) and r.get('_id') == review_id), None)
            if review:
                console.print_json(data=review)
            else:
                console.print("[yellow]Review not found[/yellow]")
        elif choice == "4":
            home_id = Prompt.ask("Home ID")
            if not home_id:
                console.print("[red]Home ID cannot be empty.[/red]")
                continue
            review_id = Prompt.ask("Review ID to update")
            if not review_id:
                console.print("[red]Review ID cannot be empty.[/red]")
                continue
            rating_input = Prompt.ask("New rating (1-5)")
            if not rating_input:
                console.print("[red]Rating cannot be empty.[/red]")
                continue
            try:
                rating = int(rating_input)
                if rating < 1 or rating > 5:
                    console.print("[red]Rating must be between 1 and 5[/red]")
                    continue
            except ValueError:
                console.print("[red]Rating must be a number[/red]")
                continue
            comment = Prompt.ask("New comment")
            if not comment:
                console.print("[red]Comment cannot be empty.[/red]")
                continue
            data = {
                'rating': rating,
                'comments': comment,
                '_id': review_id  # Add the review ID to the update data
            }
            result = make_request('PUT', f"http://localhost:8001/api/listing/{home_id}/review/{review_id}/", data)
            if result:
                console.print("[green]Review updated successfully![green]")
        elif choice == "5":
            home_id = Prompt.ask("Home ID")
            if not home_id:
                console.print("[red]Home ID cannot be empty.[/red]")
                continue
            review_id = Prompt.ask("Review ID to delete")
            if not review_id:
                console.print("[red]Review ID cannot be empty.[/red]")
                continue
            if Confirm.ask(f"Are you sure you want to delete review {review_id} for home {home_id}?"):
                result = make_request('DELETE', f"http://localhost:8001/api/listing/{home_id}/review/{review_id}/")
                if result is not None:
                    console.print("[green]Review deleted successfully![green]")
        elif choice == "0":
            break

def main_menu():
    while True:
        console.rule("[bold green]Admin Menu")
        console.print("[1] Users\n[2] Listings\n[3] Reviews\n[0] Exit")
        choice = Prompt.ask("Choose an option", choices=["1","2","3","0"])
        if choice == "1":
            user_menu()
        elif choice == "2":
            listing_menu()
        elif choice == "3":
            review_menu()
        elif choice == "0":
            console.print("[bold red]Goodbye!")
            sys.exit(0)

# Add a helper to extract user_id from the access token
def get_logged_in_user_id():
    if session.access_token:
        try:
            payload = jwt.decode(session.access_token, options={"verify_signature": False})
            return str(payload.get('user_id', ''))
        except Exception:
            return ''
    return ''

if __name__ == "__main__":
    if login():
        main_menu() 