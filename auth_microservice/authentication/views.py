from rest_framework import generics
from django.contrib.auth.models import User
from .serializers import UserSerializer
from django.core.mail import send_mail
from django.conf import settings
from django.shortcuts import get_object_or_404
from django.http import HttpResponse
import jwt

class RegisterView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer

    def perform_create(self, serializer):
        user = serializer.save(is_active=False)  # New user inactive by default

        # Generate a token with user's ID
        payload = {'user_id': user.id}
        token = jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')

        # Verification link
        verification_link = f"http://127.0.0.1:8001/api/auth/verify/{token}/"

        # Send email
        send_mail(
            'Verify your Email',
            f'Click the link to verify your email: {verification_link}',
            settings.EMAIL_HOST_USER,
            [user.email],
            fail_silently=False,
        )

class VerifyEmailView(generics.GenericAPIView):
    def get(self, request, token):
        try:
            # Decode the token
            payload = jwt.decode(token, settings.SECRET_KEY, algorithms=['HS256'])
            user = get_object_or_404(User, id=payload['user_id'])
            user.is_active = True
            user.save()
            return HttpResponse('Your email has been verified! You can now login.')
        except jwt.ExpiredSignatureError:
            return HttpResponse('Verification link has expired.', status=400)
        except jwt.exceptions.DecodeError:
            return HttpResponse('Invalid verification link.', status=400)
