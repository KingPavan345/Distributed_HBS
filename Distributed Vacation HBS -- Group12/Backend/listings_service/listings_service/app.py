"""
Listings Service Main Application
Author: Wafiul Abire Aonkon

This module contains the main Flask application for the Listings Microservice.
It handles all listing-related operations including:
- Property listings management
- Search and filtering
- Booking management
- Reviews

Key Components:
- Property CRUD operations
- Search and filter endpoints
- Booking management
- Review system
- Image handling

Dependencies:
- Flask
- SQLAlchemy
- PostgreSQL
- JWT Authentication
"""

from flask import Flask, jsonify, request
from pymongo import MongoClient
from bson.decimal128 import Decimal128
from datetime import datetime
import jwt
import requests
from functools import wraps
import os
from dotenv import load_dotenv
import logging
from bson import ObjectId
from logging.handlers import RotatingFileHandler
import re
from flask_cors import CORS

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Add rotating file handler for individual logs directory
handler = RotatingFileHandler(os.path.join(os.path.dirname(__file__), 'logs', 'listings-service.log'), maxBytes=10000000, backupCount=5)
logger.addHandler(handler)

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app, origins=["*", "http://auth-service:8000", "http://auth-service", "http://listings-service:8001", "http://listings-service"])
app.config['SECRET_KEY'] = os.getenv('FLASK_SECRET_KEY', 'vhbs@12345')

# MongoDB configuration
MONGODB_URI = os.getenv('MONGODB_URI', 'mongodb+srv://listings_user:%40l47896325O@listingms-pds.y5rsein.mongodb.net/?retryWrites=true&w=majority&appName=ListingMS-PDS')

os.makedirs(os.path.join(os.path.dirname(__file__), 'logs'), exist_ok=True)

try:
    client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    # Test the connection
    client.server_info()
    logger.info("Successfully connected to MongoDB")
    db = client["sample_airbnb"]
    homes = db["listingsAndReviews"]
except Exception as e:
    logger.error(f"Failed to connect to MongoDB: {e}")
    raise

# Auth service configuration
AUTH_SERVICE_URL = os.getenv('AUTH_SERVICE_URL', 'http://auth-service:8000')

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'message': 'Token is missing!'}), 401
        try:
            # Verify token with auth service
            auth_response = requests.get(
                f'{AUTH_SERVICE_URL}/api/auth/verify-token/',
                headers={'Authorization': token}
            )
            if auth_response.status_code != 200:
                return jsonify({'message': 'Token is invalid!'}), 401
            data = auth_response.json()
            return f(data['user_id'], *args, **kwargs)
        except:
            return jsonify({'message': 'Token is invalid!'}), 401
    return decorated

def make_serializable(obj):
    if isinstance(obj, Decimal128):
        return float(obj.to_decimal())
    if isinstance(obj, ObjectId):
        return str(obj)
    if isinstance(obj, datetime):
        return obj.isoformat()
    if isinstance(obj, list):
        return [make_serializable(i) for i in obj]
    if isinstance(obj, dict):
        return {k: make_serializable(v) for k, v in obj.items()}
    return obj

# GET all listings with pagination and filters
@app.route('/api/listings/', methods=['GET'])
def list_listings():
    try:
        page = request.args.get('page', 1, type=int)
        limit = request.args.get('limit', 10, type=int)
        property_type = request.args.get('property_type')
        country = request.args.get('country')
        min_price = request.args.get('min_price', type=float)
        max_price = request.args.get('max_price', type=float)
        min_bedrooms = request.args.get('min_bedrooms', type=int)
        sort_by = request.args.get('sort_by')
        sort_criteria = None
        if sort_by == 'price_asc':
            sort_criteria = [('price', 1)]
        elif sort_by == 'price_desc':
            sort_criteria = [('price', -1)]
        elif sort_by == 'rating_desc':
            sort_criteria = [('review_scores_rating', -1)]
        elif sort_by == 'created_at_desc':
            sort_criteria = [('calendar_last_scraped', -1)]
       
        
        # Build query based on filters
        query = {}
        if property_type:
            query['property_type'] = property_type
        if country:
            # Case-insensitive match for country
            query['address.country'] = {'$regex': f'^{re.escape(country)}$', '$options': 'i'}
        if min_price is not None:
            query['price'] = {'$gte': min_price}
        if max_price is not None:
            query['price'] = query.get('price', {})
            query['price']['$lte'] = max_price
        if min_bedrooms is not None:
            query['bedrooms'] = {'$gte': min_bedrooms}
        
        limit = min(limit, 100)
        skip = (page - 1) * limit
        
        # Get total count for pagination
        total_listings = homes.count_documents(query)
        total_pages = (total_listings + limit - 1) // limit if total_listings > 0 else 1
        page = max(1, min(page, total_pages))
        
        cursor = homes.find(query)
        if sort_criteria:
            cursor = cursor.sort(sort_criteria)
        cursor = cursor.skip(skip).limit(limit)
        
        results = []
        for listing in cursor:
            summary = {}
            summary['_id'] = str(listing.get('_id', ''))
            summary['listing_id'] = str(listing.get('listing_id', summary['_id']))
            summary['name'] = listing.get('name', '')
            summary['price'] = float(listing['price'].to_decimal()) if isinstance(listing.get('price'), Decimal128) else listing.get('price', 0)
            # Always provide a picture_url field
            picture_url = listing.get('thumbnail_url') or listing.get('picture_url')
            if not picture_url and 'images' in listing and isinstance(listing['images'], dict):
                picture_url = listing['images'].get('picture_url')
            summary['picture_url'] = picture_url or None
            summary['location'] = listing.get('address', {}).get('country', '')
            summary['bedrooms'] = listing.get('bedrooms', 0)
            summary['property_type'] = listing.get('property_type', '')
            summary['review_scores_rating'] = listing.get('review_scores_rating', None)
            results.append(summary)
        
        logger.info(f"Returning processed listings: {results}")
        
        pagination = {
            'current_page': page,
            'total_pages': total_pages,
            'total_items': total_listings,
            'items_per_page': limit,
            'has_next': page < total_pages,
            'has_prev': page > 1
        }
        
        return jsonify({
            'data': results,
            'pagination': pagination
        })
    except Exception as e:
        logger.error(f"Error in list_listings: {e}")
        return jsonify({"error": "Internal server error"}), 500

# GET a single listing by ID
@app.route('/api/listing/<listing_id>/', methods=['GET'])
def get_listing(listing_id):
    try:
        logger.info(f"🔍 Requested ID: {listing_id}")
        # Exclude reviews from the result
        listing = homes.find_one({"listing_id": listing_id}, {"reviews": 0})
        if not listing:
            listing = homes.find_one({"_id": listing_id}, {"reviews": 0})
        logger.info(f"Fetched listing from DB: {listing}")
        if listing:
            listing = make_serializable(listing)
            if 'house_rules' in listing:
                if isinstance(listing['house_rules'], list):
                    listing['house_rules'] = "\n".join(str(v) for v in listing['house_rules'])
                elif listing['house_rules'] is None:
                    listing['house_rules'] = ""
                else:
                    listing['house_rules'] = str(listing['house_rules'])
            return jsonify({"success": True, "data": listing, "message": None})
        return jsonify({"error": "Listing not found"}), 404
    except Exception as e:
        logger.error(f"Error in get_listing: {e}")
        return jsonify({"error": "Internal server error"}), 500

# GET host information
@app.route('/api/host/<host_id>/', methods=['GET'])
def get_host(host_id):
    try:
        host = homes.find_one(
            {"host.host_id": host_id},
            {"host": 1, "_id": 0}
        )
        if host and host.get('host'):
            return jsonify(host['host'])
        return jsonify({"error": "Host not found"}), 404
    except Exception as e:
        logger.error(f"Error in get_host: {e}")
        return jsonify({"error": "Internal server error"}), 500

# GET host's listings
@app.route('/api/host/<host_id>/listings/', methods=['GET'])
def get_host_listings(host_id):
    try:
        page = request.args.get('page', 1, type=int)
        limit = request.args.get('limit', 10, type=int)
        limit = min(limit, 100)
        skip = (page - 1) * limit
        
        # Get total count for pagination
        total_listings = homes.count_documents({"host.host_id": host_id})
        total_pages = (total_listings + limit - 1) // limit
        
        results = []
        for listing in homes.find(
            {"host.host_id": host_id},
            {"_id": 0}
        ).skip(skip).limit(limit):
            for key, value in listing.items():
                if isinstance(value, Decimal128):
                    listing[key] = float(value.to_decimal())
                elif key == 'house_rules':
                    if value is None:
                        listing[key] = ""
                    elif isinstance(value, list):
                        listing[key] = "\n".join(str(v) for v in value)
                    else:
                        listing[key] = str(value)
            results.append(listing)
        
        pagination = {
            'current_page': page,
            'total_pages': total_pages,
            'total_items': total_listings,
            'items_per_page': limit,
            'has_next': page < total_pages,
            'has_prev': page > 1
        }
        
        return jsonify({
            'data': results,
            'pagination': pagination
        })
    except Exception as e:
        logger.error(f"Error in get_host_listings: {e}")
        return jsonify({"error": "Internal server error"}), 500

# POST a new review
@app.route('/api/listing/<string:listing_id>/review/', methods=['POST'])
@token_required
def add_review(user_id, listing_id):
    try:
        review = request.json

        # Ensure required fields exist
        required_fields = ['_id', 'reviewer_id', 'reviewer_name', 'comments']
        for field in required_fields:
            if field not in review:
                return jsonify({"error": f"'{field}' is required"}), 400

        review['_id'] = str(review['_id'])
        review['reviewer_id'] = str(review['reviewer_id'])
        review['date'] = datetime.utcnow()
        review['listing_id'] = listing_id

        result = homes.update_one(
            {"_id": listing_id},
            {"$push": {"reviews": review}}
        )

        if result.modified_count == 1:
            return jsonify({"message": "Review added"}), 201
        return jsonify({"error": "Listing not found"}), 404
    except Exception as e:
        logger.error(f"Error in add_review: {e}")
        return jsonify({"error": "Internal server error"}), 500

# PUT (update a review)
@app.route('/api/listing/<string:listing_id>/review/<string:review_id>/', methods=['PUT'])
@token_required
def update_review(user_id, listing_id, review_id):
    try:
        # First verify the review belongs to the user
        listing = homes.find_one(
            {"_id": listing_id, "reviews._id": review_id},
            {"reviews.$": 1}
        )
        
        if not listing or not listing.get('reviews'):
            return jsonify({"error": "Review not found"}), 404
            
        if str(listing['reviews'][0]['reviewer_id']) != str(user_id):
            return jsonify({"error": "Unauthorized to update this review"}), 403

        update = request.json
        updates = {}

        if "comments" in update:
            updates["reviews.$.comments"] = update["comments"]
        if "reviewer_name" in update:
            updates["reviews.$.reviewer_name"] = update["reviewer_name"]

        updates["reviews.$.updated_at"] = datetime.utcnow()

        result = homes.update_one(
            {"_id": listing_id, "reviews._id": review_id},
            {"$set": updates}
        )

        if result.modified_count == 1:
            return jsonify({"message": "Review updated"}), 200
        return jsonify({"error": "Review not found"}), 404
    except Exception as e:
        logger.error(f"Error in update_review: {e}")
        return jsonify({"error": "Internal server error"}), 500

# DELETE a review
@app.route('/api/listing/<string:listing_id>/review/<string:review_id>/', methods=['DELETE'])
@token_required
def delete_review(user_id, listing_id, review_id):
    try:
        # First verify the review belongs to the user
        listing = homes.find_one(
            {"_id": listing_id, "reviews._id": review_id},
            {"reviews.$": 1}
        )
        
        if not listing or not listing.get('reviews'):
            return jsonify({"error": "Review not found"}), 404
            
        if str(listing['reviews'][0]['reviewer_id']) != str(user_id):
            return jsonify({"error": "Unauthorized to delete this review"}), 403

        result = homes.update_one(
            {"_id": listing_id},
            {"$pull": {"reviews": {"_id": review_id}}}
        )
        if result.modified_count == 1:
            return jsonify({"message": "Review deleted"}), 200
        return jsonify({"error": "Review not found"}), 404
    except Exception as e:
        logger.error(f"Error in delete_review: {e}")
        return jsonify({"error": "Internal server error"}), 500

# GET listing reviews
@app.route('/api/listing/<listing_id>/reviews/', methods=['GET'])
def get_listing_reviews(listing_id):
    try:
        page = request.args.get('page', 1, type=int)
        limit = request.args.get('limit', 10, type=int)
        limit = min(limit, 100)
        skip = (page - 1) * limit

        # Find the listing and only return the reviews field
        listing = homes.find_one({"listing_id": listing_id}, {"reviews": 1, "_id": 0})
        if not listing:
            listing = homes.find_one({"_id": listing_id}, {"reviews": 1, "_id": 0})
        if listing and 'reviews' in listing:
            all_reviews = listing['reviews']
            total_reviews = len(all_reviews)
            paginated_reviews = all_reviews[skip:skip+limit]
            total_pages = (total_reviews + limit - 1) // limit

            # Process house_rules as string in reviews
            for review in paginated_reviews:
                if 'house_rules' in review:
                    if isinstance(review['house_rules'], list):
                        review['house_rules'] = "\n".join(str(v) for v in review['house_rules'])
                    elif review['house_rules'] is None:
                        review['house_rules'] = ""
                    else:
                        review['house_rules'] = str(review['house_rules'])

            pagination = {
                'current_page': page,
                'total_pages': total_pages,
                'total_items': total_reviews,
                'items_per_page': limit,
                'has_next': page < total_pages,
                'has_prev': page > 1
            }
            return jsonify({"success": True, "reviews": paginated_reviews, "pagination": pagination})
        return jsonify({"success": True, "reviews": [], "pagination": {}})  # Return empty if no reviews
    except Exception as e:
        logger.error(f"Error in get_listing_reviews: {e}")
        return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    port = int(os.getenv('PORT', 8001))
    logger.info(f"Starting server on port {port}")
    app.run(host='0.0.0.0', port=port, debug=True)
