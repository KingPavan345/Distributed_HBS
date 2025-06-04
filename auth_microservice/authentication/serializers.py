from rest_framework import serializers
from django.contrib.auth.models import User
import re

class UserSerializer(serializers.ModelSerializer):
    email = serializers.EmailField(required=True)

    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password']
        extra_kwargs = {'password': {'write_only': True}}

    def validate_password(self, value):
        # Password অবশ্যই strong হতে হবে
        if len(value) < 8:
            raise serializers.ValidationError("Password must be at least 8 characters long.")
        if not re.search(r'\d', value):
            raise serializers.ValidationError("Password must contain at least one number.")
        if not re.search(r'[A-Z]', value):
            raise serializers.ValidationError("Password must contain at least one uppercase letter.")
        return value

    def validate(self, data):
        username = data.get('username')
        email = data.get('email')

        # এখন শুধুমাত্র active user থাকলে তবেই block করবে
        if User.objects.filter(username=username, is_active=True).exists():
            raise serializers.ValidationError({"username": "This username is already taken and active."})
        if User.objects.filter(email=email, is_active=True).exists():
            raise serializers.ValidationError({"email": "This email is already taken and active."})
        return data

    def create(self, validated_data):
        user = User(
            username=validated_data['username'],
            email=validated_data['email'],
            is_active=False  # Email verification এর আগে inactive
        )
        user.set_password(validated_data['password'])
        user.save()
        return user



